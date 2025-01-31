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
public class JSONSerializer implements Serializator {
    private static final Logger log = Logger.getLogger(JSONSerializer.class.getName());
    @Inject
    private TypeConverterRegistry typeConverterRegistry;

    public JSONSerializer() {
    }

    @Override
    public String serialize(Object obj) {
        if (isNotSerializable(obj)) {
            log.severe("Class " + obj.getClass().getName() + " is not market Serializable");
            return null;
        }

        if (isIterableOrArray(obj)) {
            return serializeJsonArray(obj);
        }
        return serializeJsonObject(obj);
    }

    @Override
    public <T> T deserialize(String json, Class<T> classOfT) {
        if (!isJsonObj(json)) {
            throw new RuntimeException("Invalid json: " + json);
        }
        try {
            if (Collection.class.isAssignableFrom(classOfT)) {
                throw new RuntimeException("Cannot map directly to collection, wrap collection in type token");
            }
            return deserializeJsonObject(json, classOfT);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.severe("constructor not found for class " + classOfT.getName());
            throw new RuntimeException(e);
        }
    }

    private <T> T deserializeJsonObject(String json, Class<T> classOfT) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        T instance;
        Constructor<T> declaredConstructor = classOfT.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        instance = declaredConstructor.newInstance();
        instance = setFieldsOfJsonObjectInstance(instance, json);
        return instance;
    }

    private <T> T setFieldsOfJsonObjectInstance(T instance, String json) {
        String compressedJson = StringUtils.removeWhiteSpace(json);
        String unwrappedJson = StringUtils.removeFirstAndLastCharacter(compressedJson);
        String[] keyValuePairs = StringUtils.splitPreservingQuotesAndBrackets(unwrappedJson, ',');
        for (String keyValuePair : keyValuePairs) {
            setField(keyValuePair, instance);
        }
        return instance;
    }

    boolean isNotSerializable(Object obj) {
        return !(obj instanceof Serializable);
    }

    boolean isIterableOrArray(Object obj) {
        return obj instanceof Iterable<?> || obj.getClass().isArray();
    }


    private String serializeJsonObject(Object obj) {
        List<Field> fields = getNonSyntheticFields(obj);
        List<String> keyValuePairs = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String keyValuePair = turnFieldToJSONKeyValuePair(obj, field);
                keyValuePairs.add(keyValuePair);
            } catch (IllegalAccessException e) {
                log.severe("field " + field.getName() + " not found");
            }
        }
        String joinedKeyValuePairs = String.join(",", keyValuePairs);
        return "{" + joinedKeyValuePairs + "}";
    }

    private String turnFieldToJSONKeyValuePair(Object obj, Field field) throws RuntimeException, IllegalAccessException {
        Object fieldInstance = field.get(obj);
        String fieldName = getSerializedNameOfField(field);
        return stringifyKeyValuePair(fieldInstance, fieldName);
    }

    //todo
    private String serializeJsonArray(Object obj) {
        List<String> arrayElements = new ArrayList<>();

        if (obj instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                String arrayElement = convertObjectToJson(element);
                arrayElements.add(arrayElement);
            }
        } else {
            // Process Array
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object o = Array.get(obj, i);
                String arrayElement = convertObjectToJson(o);
                arrayElements.add(arrayElement);
            }
        }
        String joinedArrayElements = String.join(",", arrayElements);
        return "[" + joinedArrayElements + "]";
    }


    private String getSerializedNameOfField(Field field) {
        if (field.isAnnotationPresent(JsonValue.class)) {
            JsonValue annotation = field.getAnnotation(JsonValue.class);
            return annotation.value();
        }
        return field.getName();
    }

    private String stringifyKeyValuePair(Object fieldInstance, String fieldName) {
        String stringFomat = "\"" + fieldName + "\":%s";
        String converted = convertObjectToJson(fieldInstance);
        return String.format(stringFomat, converted);
    }


    private boolean isJsonObj(String str) {
        return (str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}') ||
                (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']');

    }


    private <T> void setField(String fieldValueStr, T instance) {
        String[] keyValuePair = fieldValueStr.split(":", 2);
        String key = StringUtils.removeQuotes(keyValuePair[0].trim());
        String value = isJsonObj(keyValuePair[1]) ?
                keyValuePair[1] :
                keyValuePair[1].trim().replaceAll("\"", "");
        try {
            Field field = getField(instance, key);
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
                    Object o = deserialize(value, field.getType());
                    field.set(instance, o);
                }
            }
        } catch (IllegalAccessException e) {
            log.severe("Illegal access on field " + key + " in class " + instance.getClass().getName());
            throw new RuntimeException(e);
        }
    }

    private static <T> Field getField(T instance, String fieldName) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(JsonValue.class)) {
                if (field.getAnnotation(JsonValue.class).value().equals(fieldName)) {
                    return field;
                }
            } else if (field.getName().equals(fieldName)) {
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

    public String convertObjectToJson(Object value) {
        if (value == null) {
            return "null";
        }
        String result = typeConverterRegistry.convert(value, String.class).orElseGet(() -> serialize(value));

        if (shouldWrapInQuotes(value)) {
            return '"' + result + '"';
        } else {
            return result;
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