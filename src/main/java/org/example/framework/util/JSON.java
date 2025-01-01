package org.example.framework.util;

import org.example.framework.annotations.JsonValue;

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
        if (obj instanceof Iterable<?> || obj.getClass().isArray()) {
            out.append("[");

            // Check if the object is an Iterable or an array
            if (obj instanceof Iterable<?>) {
                // Process Iterable
                Iterator<?> iterator = ((Iterable<?>) obj).iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    Optional<String> converted = TypeConverter.convertJsonStringify(element);
                    out.append(converted.orElseGet(() -> toJson(element)));
                    out.append(",");
                }
            } else {
                // Process Array
                int length = Array.getLength(obj);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(obj, i);
                    Optional<String> converted = TypeConverter.convertJsonStringify(element);
                    out.append(converted.orElseGet(() -> toJson(element)));
                    out.append(",");
                }
            }

            // Remove the last comma (if any)
            if (out.charAt(out.length() - 1) == ',') {
                out.setLength(out.length() - 1);
            }

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
        String stringFomat = "\"" + fieldName + "\":%s";
        if (fieldInstance == null) {
            stringFomat = "\"" + fieldName + "\":%s";
            return String.format(stringFomat, "null");
        }
        Optional<String> converted = TypeConverter.convertJsonStringify(fieldInstance);
        if (converted.isPresent()) {
            return String.format(stringFomat, converted.get());
        } else {
            return String.format(stringFomat, JSON.toJson(fieldInstance));
        }
    }


    private static boolean isJsonObj(String str) {
        return (str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}') ||
                (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']');

    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        StringBuffer sb = new StringBuffer(StringUtils.removeWhiteSpace(json));

        T instance;
        if (!isJsonObj(sb.toString())) {
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
        String value = isJsonObj(keyValue[1]) ?
                keyValue[1] :
                keyValue[1].trim().replaceAll("\"", "");
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