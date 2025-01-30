package org.example.framework.util;

import org.example.framework.core.annotations.Component;
import org.example.framework.core.annotations.Inject;
import org.example.framework.util.type.TypeConverterRegistry;
import org.example.framework.web.annotations.JsonValue;

import java.io.Serializable;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

@Component
public class JSONSerializer {
    private static final Logger log = Logger.getLogger(JSONSerializer.class.getName());
    @Inject
    private TypeConverterRegistry typeConverterRegistry;

    public JSONSerializer() {
    }

    private String resolveName(Field field) {
        if (field.isAnnotationPresent(JsonValue.class)) {
            JsonValue annotation = field.getAnnotation(JsonValue.class);
            return annotation.value();
        }
        return field.getName();
    }

    public String toJson(Object obj) {
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
                for (Object element : (Iterable<?>) obj) {
                    Optional<String> converted = convertJsonStringify(element);
                    out.append(converted.orElseGet(() -> toJson(element)));
                    out.append(",");
                }
            } else {
                // Process Array
                int length = Array.getLength(obj);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(obj, i);
                    Optional<String> converted = convertJsonStringify(element);
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


    private String resolveFieldToJSONString(Object fieldInstance, String fieldName) {
        String stringFomat = "\"" + fieldName + "\":%s";
        if (fieldInstance == null) {
            stringFomat = "\"" + fieldName + "\":%s";
            return String.format(stringFomat, "null");
        }
        Optional<String> converted = convertJsonStringify(fieldInstance);
        if (converted.isPresent()) {
            return String.format(stringFomat, converted.get());
        } else {
            return String.format(stringFomat, toJson(fieldInstance));
        }
    }


    private boolean isJsonObj(String str) {
        return (str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}') ||
                (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']');

    }

    public <T> T fromJson(String json, Class<T> classOfT) {
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

    private <T> void setField(String fieldValueStr, T instance) {
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
                Optional<?> parsedValue = typeConverterRegistry.convert(value, field.getType());
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

    private <T> List<T> parseJsonArray(String jsonArray, Class<?> elementType) {
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[") && jsonArray.endsWith("]")) {
            jsonArray = jsonArray.substring(1, jsonArray.length() - 1).trim();
        }

        String[] elements = StringUtils.splitPreservingQuotesAndBrackets(jsonArray, ',');
        List<T> list = new ArrayList<>();

        for (String element : elements) {
            T item = (T) typeConverterRegistry.convert(element, elementType);
            list.add(item);
        }

        return list;
    }
    public Optional<String> convertJsonStringify(Object value) {
        if (value == null) {
            return Optional.of("null"); // Or handle as per your use case, e.g., throw an exception
        }

        Class<?> type = value.getClass();
        // If the type has a registered converter in TO_STRING_CONVERTERS
        if (typeConverterRegistry.hasConverter(type, String.class)) {
            // Use the registered converter for the type
            String result = typeConverterRegistry.convert(value, String.class).orElseThrow(() -> new RuntimeException("Cannot convert " + value + " to string"));

            if (shouldWrapInQuotes(value)) {
                return Optional.of("\"" + result + "\"");
            } else {
                return Optional.ofNullable(result);
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean shouldWrapInQuotes(Object obj) {
        if (obj == null) {
            return false; // null is not wrapped in quotes
        }

        Class<?> clazz = obj.getClass();

        // Strings, characters, enums, dates, and other "string-convertible" types
        if (obj instanceof String ||
                obj instanceof Character ||
                clazz.isEnum() ||
                obj instanceof Date ||
                obj instanceof UUID ||
                obj instanceof Currency ||
                obj instanceof Locale ||
                obj instanceof LocalDate ||
                obj instanceof LocalDateTime ||
                obj instanceof LocalTime ||
                obj instanceof TimeZone) {
            return true;
        }

        // Numbers and booleans are not wrapped in quotes
        if (obj instanceof Number || obj instanceof Boolean) {
            return false;
        }

        // Collections, arrays, and objects are typically serialized without quotes
        return false;
    }
}