package org.example.framework.web;

import org.example.framework.annotations.JsonValue;
import org.example.framework.util.Mapper;
import org.example.framework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.time.temporal.Temporal;
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
            out.append("[\n");
            List<String> subjsons = new ArrayList<>();
            for (Object o : iterable) {
                subjsons.add(toJson(o));
            }
            String join = String.join(",", subjsons);
            out.append(join);
            out.append("]\n");
        } else {
            out.append("{\n");
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                //craete an instance of the field
                try {
                    Object fieldInstance = field.get(obj);
                    String fieldName = resolveName(field);
                    String stringFomat = "\"" + fieldName + "\": \"%s\"";
                    //if its the last field omit the ","
                    if (i != fields.length - 1) {
                        stringFomat += ",\n";
                    } else {
                        stringFomat += "\n";
                    }
                    switch (fieldInstance) {
                        case null -> out.append(String.format(stringFomat, "null"));
                        case Number number -> out.append(String.format(stringFomat, number.toString()));
                        case CharSequence charSequence -> out.append(String.format(stringFomat, charSequence));
                        case Temporal date -> out.append(String.format(stringFomat, date.toString()));
                        case Date date -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            out.append(String.format(sdf.format(date)));
                        }
                        default -> out.append(String.format("\"%s\": %s", fieldName, JSON.toJson(fieldInstance)));
                    }
                } catch (IllegalAccessException e) {
                    log.severe("field " + field.getName() + " not found");
                    e.printStackTrace();
                }
            }
            out.append("}\n");
        }
        return out.toString();
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
//        if (!Arrays.stream(classOfT.getInterfaces()).anyMatch(aClass -> aClass.equals(Serializable.class))) {
//            log.severe("class" + classOfT.getName() + " is not Serializable");
//            throw new RuntimeException("Not serializable");
//        }
        StringBuffer sb = new StringBuffer(StringUtils.removeWhiteSpace(json));

        T instance;
        try {
            Constructor<T> declaredConstructor = classOfT.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            instance = declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.severe("constructor not found for class " + classOfT.getName());
            throw new RuntimeException(e);
        }

        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.length() - 1);
        for (String pair : StringUtils.splitPreservingQuotesAndBrackets(sb.toString(), ',')) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replaceAll("\"", ""); // Remove quotes
            String value = keyValue[1].trim().replaceAll("\"", "");
            System.out.println(String.format("Key: %s, Value: %s", key, value));
            // Find the corresponding field in the class
            setField(key, value, instance);
        }

        return instance;
    }

    private static <T> void setField(String key, String value, T instance) {
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
                Object parsedValue = Mapper.mapStringToType(value, instance.getClass()).orElse(fromJson(value, field.getClass()));
                field.set(instance, parsedValue);
            }
        } catch (IllegalAccessException e) {
            log.

                    severe("Illegal access on field " + key + " in class " + instance.getClass().

                            getName());
            throw new

                    RuntimeException(e);
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
            T item = (T) Mapper.mapStringToType(element, elementType).get(); // Use the helper method
            list.add(item);
        }

        return list;
    }
}