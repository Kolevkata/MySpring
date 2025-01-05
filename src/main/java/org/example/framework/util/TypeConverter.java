package org.example.framework.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class TypeConverter {
    private static final Map<Class<?>, Function<String, ?>> FROM_STRING_CONVERTERS = new HashMap<>();
    private static final Map<Class<?>, Function<?, String>> TO_STRING_CONVERTERS = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    static {
        initializeConverters();
    }

    private static void initializeConverters() {
        // FROM STRING CONVERTERS
        // Primitive and wrapper types
        registerFromStringConverter(String.class, s -> s.replaceAll("\"", ""));
        registerFromStringConverter(Integer.class, Integer::valueOf);
        registerFromStringConverter(int.class, Integer::valueOf);
        registerFromStringConverter(Long.class, Long::valueOf);
        registerFromStringConverter(long.class, Long::valueOf);
        registerFromStringConverter(Double.class, Double::valueOf);
        registerFromStringConverter(double.class, Double::valueOf);
        registerFromStringConverter(Float.class, Float::valueOf);
        registerFromStringConverter(float.class, Float::valueOf);
        registerFromStringConverter(Boolean.class, Boolean::valueOf);
        registerFromStringConverter(boolean.class, Boolean::valueOf);
        registerFromStringConverter(Byte.class, Byte::valueOf);
        registerFromStringConverter(byte.class, Byte::valueOf);
        registerFromStringConverter(Short.class, Short::valueOf);
        registerFromStringConverter(short.class, Short::valueOf);
        registerFromStringConverter(Character.class, s -> s.charAt(0));
        registerFromStringConverter(char.class, s -> s.charAt(0));

        // Big number types
        registerFromStringConverter(BigInteger.class, BigInteger::new);
        registerFromStringConverter(BigDecimal.class, BigDecimal::new);

        // Date and Time types
        registerFromStringConverter(LocalDate.class, s -> LocalDate.parse(s, DATE_FORMATTER));
        registerFromStringConverter(LocalDateTime.class, s -> LocalDateTime.parse(s, DATE_TIME_FORMATTER));
        registerFromStringConverter(LocalTime.class, LocalTime::parse);
        registerFromStringConverter(ZonedDateTime.class, ZonedDateTime::parse);
        registerFromStringConverter(OffsetDateTime.class, OffsetDateTime::parse);
        registerFromStringConverter(Instant.class, Instant::parse);
        registerFromStringConverter(Year.class, Year::parse);
        registerFromStringConverter(YearMonth.class, YearMonth::parse);
        registerFromStringConverter(MonthDay.class, MonthDay::parse);
        registerFromStringConverter(Date.class, s -> Date.from(Instant.parse(s)));

        // Utility types
        registerFromStringConverter(UUID.class, UUID::fromString);
        registerFromStringConverter(Currency.class, Currency::getInstance);
        registerFromStringConverter(Locale.class, Locale::forLanguageTag);
        registerFromStringConverter(TimeZone.class, TimeZone::getTimeZone);

        // TO STRING CONVERTERS
        // Primitive and wrapper types
        registerToStringConverter(String.class, Object::toString);
        registerToStringConverter(Integer.class, Object::toString);
        registerToStringConverter(int.class, Object::toString);
        registerToStringConverter(Long.class, Object::toString);
        registerToStringConverter(long.class, Object::toString);
        registerToStringConverter(Double.class, Object::toString);
        registerToStringConverter(double.class, Object::toString);
        registerToStringConverter(Float.class, Object::toString);
        registerToStringConverter(float.class, Object::toString);
        registerToStringConverter(Boolean.class, Object::toString);
        registerToStringConverter(boolean.class, Object::toString);
        registerToStringConverter(Byte.class, Object::toString);
        registerToStringConverter(byte.class, Object::toString);
        registerToStringConverter(Short.class, Object::toString);
        registerToStringConverter(short.class, Object::toString);
        registerToStringConverter(Character.class, Object::toString);
        registerToStringConverter(char.class, Object::toString);

        // Big number types
        registerToStringConverter(BigInteger.class, BigInteger::toString);
        registerToStringConverter(BigDecimal.class, BigDecimal::toString);

        // Date and Time types
        registerToStringConverter(LocalDate.class, date -> ((LocalDate) date).format(DATE_FORMATTER));
        registerToStringConverter(LocalDateTime.class, dt -> ((LocalDateTime) dt).format(DATE_TIME_FORMATTER));
        registerToStringConverter(LocalTime.class, LocalTime::toString);
        registerToStringConverter(ZonedDateTime.class, ZonedDateTime::toString);
        registerToStringConverter(OffsetDateTime.class, OffsetDateTime::toString);
        registerToStringConverter(Instant.class, Instant::toString);
        registerToStringConverter(Year.class, Year::toString);
        registerToStringConverter(YearMonth.class, YearMonth::toString);
        registerToStringConverter(MonthDay.class, MonthDay::toString);
        registerToStringConverter(Date.class, date -> ((Date) date).toInstant().toString());

        // Utility types
        registerToStringConverter(UUID.class, UUID::toString);
        registerToStringConverter(Currency.class, Currency::getCurrencyCode);
        registerToStringConverter(Locale.class, Locale::toLanguageTag);
        registerToStringConverter(TimeZone.class, TimeZone::getID);
    }

    /**
     * Converts a value from one type to another using string as an intermediate format if necessary.
     *
     * @param value      The value to convert
     * @param targetType The target class type
     * @param <S>        The source type parameter
     * @param <T>        The target type parameter
     * @return An Optional containing the converted value, or empty if conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <S, T> Optional<T> convert(S value, Class<T> targetType) {
        if (value == null) {
            return Optional.empty();
        }
        Class<?> sourceType = value.getClass();

        // If source and target are the same, just cast
        if (sourceType.equals(targetType)) {
            return Optional.of((T) value);
        }

        try {
            // If target type is String, use TO_STRING_CONVERTER
            if (targetType.equals(String.class)) {
                Function<S, String> toStringConverter = (Function<S, String>) TO_STRING_CONVERTERS.get(sourceType);
                if (toStringConverter != null) {
                    return Optional.of((T) toStringConverter.apply(value));
                }
            }

            // If source type is String, use FROM_STRING_CONVERTER
            if (sourceType.equals(String.class)) {
                Function<String, T> fromStringConverter = (Function<String, T>) FROM_STRING_CONVERTERS.get(targetType);
                if (fromStringConverter != null) {
                    return Optional.of(fromStringConverter.apply((String) value));
                }
            }

            // For other type combinations, use String as intermediate
            Function<S, String> toStringConverter = (Function<S, String>) TO_STRING_CONVERTERS.get(sourceType);
            Function<String, T> fromStringConverter = (Function<String, T>) FROM_STRING_CONVERTERS.get(targetType);

            if (toStringConverter != null && fromStringConverter != null) {
                String intermediateString = toStringConverter.apply(value);
                return Optional.of(fromStringConverter.apply(intermediateString));
            }

            // Handle enum types
            if (sourceType.isEnum() && targetType.equals(String.class)) {
                return Optional.of((T) ((Enum<?>) value).name());
            }
            if (sourceType.equals(String.class) && targetType.isEnum()) {
                return Optional.of((T) Enum.valueOf((Class<Enum>) targetType, (String) value));
            }

            return Optional.empty();
        } catch (Exception e) {
            // Log the error if needed
            // logger.debug("Failed to convert {} from {} to {}: {}", value, sourceType, targetType, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Registers a custom converter from String to a specific type.
     */
    public static <T> void registerFromStringConverter(Class<T> clazz, Function<String, T> converter) {
        FROM_STRING_CONVERTERS.put(clazz, converter);
    }

    /**
     * Registers a custom converter from a specific type to String.
     */
    public static <T> void registerToStringConverter(Class<T> clazz, Function<T, String> converter) {
        TO_STRING_CONVERTERS.put(clazz, (Function<?, String>) converter);
    }


    /**
     * Converts an object to a JSON-compatible string representation.
     * If the type should be stringified, it will return a string, otherwise return a default or error value.
     *
     * @param value The object to convert.
     * @return The stringified JSON representation of the object, or null if not applicable.
     */
    public static Optional<String> convertJsonStringify(Object value) {
        if (value == null) {
            return Optional.of("null"); // Or handle as per your use case, e.g., throw an exception
        }

        Class<?> type = value.getClass();
        // If the type has a registered converter in TO_STRING_CONVERTERS
        if (TypeConverter.TO_STRING_CONVERTERS.containsKey(type)) {
            // Use the registered converter for the type
            Function<Object, String> toStringConverter = (Function<Object, String>) TypeConverter.TO_STRING_CONVERTERS.get(type);
            String result = toStringConverter.apply(value);

            if (shouldWrapInQuotes(value)) {
                return Optional.of("\"" + result + "\"");
            } else {
                return Optional.ofNullable(result);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks whether the given object's type should be wrapped in quotation marks in JSON.
     *
     * @param obj The object to check.
     * @return true if the type should be wrapped in quotes, false otherwise.
     */
    private static boolean shouldWrapInQuotes(Object obj) {
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