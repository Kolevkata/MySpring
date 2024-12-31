package org.example.framework.web;

import org.example.framework.annotations.JsonValue;
import org.example.framework.util.Mapper;
import org.example.framework.util.StringUtils;
import org.example.framework.util.TypeConverter;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public class JSON {
    private static final Logger log = Logger.getLogger(JSON.class.getName());

    private JSON() {
    }

    private static String resolveName(Field field) {
        if (field.isAnnotationPresent(JsonValue.class)) {
            JsonValue annotation = field.getAnnotation(JsonValue.class);
            return annotation.value();
        }
        return field.getName();
    }

    public static String toJson(Object obj) {
        if (!(obj instanceof Serializable)) {
            log.severe("Class " + obj.getClass().getName() + " is not market Serializable");
            return null;
        }

        StringBuilder out = new StringBuilder();
        if (obj instanceof Iterable<?> iterable) {
            out.append("[");
            List<String> subjsons = new ArrayList<>();
            for (Object o : iterable) {
                Optional<String> converted = TypeConverter.convert(o, String.class);
                if (converted.isPresent()) {
                    subjsons.add("\"" + converted.get() + "\"");
                } else {
                    subjsons.add(toJson(o));
                }
            }
            String join = String.join(",", subjsons);
            out.append(join);
            out.append("]");
        } else {
            out.append("{");
            List<Field> fields = Arrays.stream(obj.getClass().getDeclaredFields()).filter(field -> !field.isSynthetic()).toList();
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true);
                try {
                    Object fieldInstance = field.get(obj);
                    String fieldName = resolveName(field);
                    out.append(resolveFieldToJSONString(fieldInstance, fieldName));
                    field.setAccessible(true);
                    if (i != fields.size() - 1) {
                        out.append(",");
                    }
                } catch (IllegalAccessException e) {
                    log.severe("field " + field.getName() + " not found");
                    e.printStackTrace();
                }
            }
            out.append("}");
        }
        return out.toString();
    }


    private static String resolveFieldToJSONString(Object fieldInstance, String fieldName) {
        String stringFomat;
        if (fieldInstance == null) {
            stringFomat = "\"" + fieldName + "\":%s";
            return String.format(stringFomat, "null");
        }
        Optional<String> converted = TypeConverter.convert(fieldInstance, String.class);
        if (converted.isPresent()) {
            stringFomat = "\"" + fieldName + "\":\"%s\"";
            return String.format(stringFomat, converted.get());
        } else {
            stringFomat = "\"" + fieldName + "\":%s";
            return String.format(stringFomat, JSON.toJson(fieldInstance));
        }
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
        StringBuffer sb = new StringBuffer(StringUtils.removeWhiteSpace(json));

        T instance;
        if (!((sb.charAt(0) == '{' && sb.charAt(sb.length() - 1) == '}') ||
                (sb.charAt(0) == '[' && sb.charAt(sb.length() - 1) == ']'))) {
            throw new RuntimeException("Invalid json string");
        }
        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.length() - 1);
        String[] pairs = StringUtils.splitPreservingQuotesAndBrackets(sb.toString(), ',');
        try {
            if (Collection.class.isAssignableFrom(classOfT)) {
                throw new RuntimeException("Cannot map directly to collection, wrap collection in type token");
            }

            Constructor<T> declaredConstructor = classOfT.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            instance = declaredConstructor.newInstance();
            for (String pair : pairs) {
                setField(pair, instance);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.severe("constructor not found for class " + classOfT.getName());
            throw new RuntimeException(e);
        }
        return instance;
    }

    private static <T> void setField(String fieldValueStr, T instance) {
        String[] keyValue = fieldValueStr.split(":", 2);
        String key = keyValue[0].trim().replaceAll("\"", ""); // Remove quotes
        String value = keyValue[1].trim().replaceAll("\"", "");
        try {
            Field field = resolveField(instance, key);

            field.setAccessible(true);
            // Check if field is a collection type
            if (Collection.class.isAssignableFrom(field.getType())) {
                // Parse as collection using parseJsonArray
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> elementType = (Class<?>) genericType.getActualTypeArguments()[0];
                List<?> parsedList = parseJsonArray(value, elementType);
                field.set(instance, parsedList);
            } else {
                Optional<?> parsedValue = TypeConverter.convert(value, field.getType());
                if (parsedValue.isPresent()) {
                    field.set(instance, parsedValue.get());
                } else {
                    Object o = fromJson(value, field.getType());
                    field.set(instance, o);
                }
            }
        } catch (IllegalAccessException e) {
            log.severe("Illegal access on field " + key + " in class " + instance.getClass().getName());
            throw new RuntimeException(e);
        }
    }

    private static <T> Field resolveField(T instance, String key) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(JsonValue.class)) {
                if (field.getAnnotation(JsonValue.class).value().equals(key)) {
                    return field;
                }
            } else if (field.getName().equals(key)) {
                return field;
            }
        }
        return null;
    }

    private static <T> List<T> parseJsonArray(String jsonArray, Class<?> elementType) {
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[") && jsonArray.endsWith("]")) {
            jsonArray = jsonArray.substring(1, jsonArray.length() - 1).trim();
        }

        String[] elements = StringUtils.splitPreservingQuotesAndBrackets(jsonArray, ',');
        List<T> list = new ArrayList<>();

        for (String element : elements) {
            T item = (T) TypeConverter.convert(element, elementType).get();
            list.add(item);
        }

        return list;
    }
}