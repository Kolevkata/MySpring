package org.example.framework.web;

import org.example.framework.annotations.JsonValue;

import java.io.Serializable;
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

//    public static String toJson(Field obj) {
//
//    }

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
        StringBuffer sb = new StringBuffer(removeWhiteSpace(json));

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
        for (String pair : splitPreservingQuotesAndBrackets(sb.toString(), ',')) {
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
                // Use the parseElement method for non-collection fields
                Object parsedValue = parseElement(value, field.getType());
                field.set(instance, parsedValue);
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

    private static <T> T parseElement(String element, Class<?> elementType) {
        if (elementType == String.class) {
            return (T) element.replaceAll("\"", ""); // Remove quotes from String
        } else if (elementType == Integer.class || elementType == int.class) {
            return (T) Integer.valueOf(element); // Parse Integer
        } else if (elementType == Double.class || elementType == double.class) {
            return (T) Double.valueOf(element); // Parse Double
        } else if (elementType == Boolean.class || elementType == boolean.class) {
            return (T) Boolean.valueOf(element); // Parse Boolean
        } else if (elementType == LocalDate.class) {
            return (T) parseLocalDate(element); // Parse LocalDate
        } else if (elementType == LocalDateTime.class) {
            return (T) parseLocalDateTime(element); // Parse LocalDateTime
        } else if (elementType == Date.class) {
            return (T) parseDate(element); // Parse LocalDateTime
        } else {
            return (T) fromJson(element, elementType); // Recursive call for nested objects
        }
    }


    private static Object parseDate(String value) {
        try {
            // Remove quotes and parse the date
            value = value.replaceAll("\"", "").trim();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            return dateFormat.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse date: " + value, e);
        }
    }

    private static Object parseNumber(String value, Class<?> fieldType) {
        if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(value);
        } else if (fieldType == short.class || fieldType == Short.class) {
            return Short.parseShort(value);
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            return Byte.parseByte(value);
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + fieldType);
        }
    }

    private static <T> List<T> parseJsonArray(String jsonArray, Class<?> elementType) {
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[") && jsonArray.endsWith("]")) {
            jsonArray = jsonArray.substring(1, jsonArray.length() - 1).trim();
        }

        String[] elements = splitPreservingQuotesAndBrackets(jsonArray, ',');
        List<T> list = new ArrayList<>();

        for (String element : elements) {
            T item = parseElement(element, elementType); // Use the helper method
            list.add(item);
        }

        return list;
    }

    private static String removeWhiteSpace(String input) {
        StringBuilder output = new StringBuilder();
        boolean insideQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes; // Toggle insideQuotes when encountering a quote
            }

            // Append characters, keeping spaces only if inside quotes
            if (!insideQuotes && Character.isWhitespace(c)) {
                continue; // Skip whitespace outside quotes
            }

            output.append(c);
        }
        System.out.println(output);
        return output.toString();
    }


    // More readable solution using Pattern matching
    public static String[] splitPreservingQuotesAndBrackets(String str, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        Stack<Character> brackets = new Stack<>();

        for (char c : str.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == '{' || c == '[') {
                brackets.push(c);
                current.append(c);
            } else if (c == '}' || c == ']') {
                brackets.pop();
                current.append(c);
            } else if (c == delimiter && !inQuotes && brackets.isEmpty()) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result.toArray(new String[0]);
    }

    private static LocalDate parseLocalDate(String value) {
        try {
            // Remove quotes and parse the LocalDate
            value = value.replaceAll("\"", "").trim();
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse LocalDate: " + value, e);
        }
    }

    private static LocalDateTime parseLocalDateTime(String value) {
        try {
            // Remove quotes and parse the LocalDateTime
            value = value.replaceAll("\"", "").trim();
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse LocalDateTime: " + value, e);
        }
    }
}