package org.example.framework.util.type;

import org.example.framework.core.annotations.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Component
@SuppressWarnings("rawtypes")
public class TypeConverterRegistry {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Set<Converter> converters;

    public TypeConverterRegistry() {
        this.converters = new HashSet<>();
        initializeConverters();
    }

    public <T, S> Optional<S> convert(T source, Class<S> targetType) throws RuntimeException {
        Optional<Converter> converter = findConverter(source.getClass(), targetType);
        if (converter.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((S) converter.get().convert(source));
    }

    private Optional<Converter> findConverter(Class<?> sourceType, Class<?> targetType) {
        for (Converter converter : converters) {
            if (sourceAndTargetAreEqual(converter, sourceType, targetType)) {
                return Optional.of(converter);
            }
        }
        return Optional.empty();
    }

    public <T, S> void registerConverter(Class<T> sourceType, Class<S> targetType, Function<T, S> convertionFunction) {
        Converter<T, S> converter = new Converter<>(sourceType, targetType, convertionFunction);
        converters.add(converter);
    }

    public <T, S> void removeConverter(Class<T> sourceType, Class<S> targetType) {
        converters.removeIf(s -> sourceAndTargetAreEqual(s, sourceType, targetType));
    }


    public boolean hasConverter(Class<?> sourceClass, Class<?> destinationClass) {
        return findConverter(sourceClass, destinationClass).isPresent();
    }


    private <T, S> boolean sourceAndTargetAreEqual(Converter converter, Class<T> sourceType, Class<S> targetType) {
        return converter.getSourceType().equals(sourceType) && converter.getTargetType().equals(targetType);
    }

    private void initializeConverters() {
        // FROM STRING CONVERTERS
        registerPrimitivesAndWrappers();
        registerBigNumberTypes();
        registerDateTimeTypes();
        registerUtilityTypes();
    }

    private void registerPrimitivesAndWrappers() {
        registerFromStringToPrimitivesAndWrappers();
        registerFromPrimitivesAndWrappersToString();
    }

    private void registerBigNumberTypes() {
        registerFromStringToBigNumberTypes();
        registerFromBigNumbersToString();
    }

    private void registerDateTimeTypes() {
        registerFromStringToDateTimeTypes();
        registerDateTimeTypesToString();
    }

    private void registerUtilityTypes() {
        registerFromStringToUtilityTypes();
        registerUtilityTypesToString();
    }


    private void registerFromStringToDateTimeTypes() {
        // Date and Time types
        registerConverter(String.class, LocalDate.class, s -> LocalDate.parse(s, DATE_FORMATTER));
        registerConverter(String.class, LocalDateTime.class, s -> LocalDateTime.parse(s, DATE_TIME_FORMATTER));
        registerConverter(String.class, LocalTime.class, LocalTime::parse);
        registerConverter(String.class, ZonedDateTime.class, ZonedDateTime::parse);
        registerConverter(String.class, OffsetDateTime.class, OffsetDateTime::parse);
        registerConverter(String.class, Instant.class, Instant::parse);
        registerConverter(String.class, Year.class, Year::parse);
        registerConverter(String.class, YearMonth.class, YearMonth::parse);
        registerConverter(String.class, MonthDay.class, MonthDay::parse);
        registerConverter(String.class, Date.class, s -> Date.from(Instant.parse(s)));
    }

    private void registerFromPrimitivesAndWrappersToString() {
        // Primitive and wrapper types
        registerConverter(String.class, String.class, Object::toString);
        registerConverter(Integer.class, String.class, Object::toString);
        registerConverter(int.class, String.class, Object::toString);
        registerConverter(Long.class, String.class, Object::toString);
        registerConverter(long.class, String.class, Object::toString);
        registerConverter(Double.class, String.class, Object::toString);
        registerConverter(double.class, String.class, Object::toString);
        registerConverter(Float.class, String.class, Object::toString);
        registerConverter(float.class, String.class, Object::toString);
        registerConverter(Boolean.class, String.class, Object::toString);
        registerConverter(boolean.class, String.class, Object::toString);
        registerConverter(Byte.class, String.class, Object::toString);
        registerConverter(byte.class, String.class, Object::toString);
        registerConverter(Short.class, String.class, Object::toString);
        registerConverter(short.class, String.class, Object::toString);
        registerConverter(Character.class, String.class, Object::toString);
        registerConverter(char.class, String.class, Object::toString);
    }

    private void registerFromStringToPrimitivesAndWrappers() {
        // Primitive and wrapper types
        registerConverter(String.class, String.class, s -> s);
        registerConverter(String.class, Integer.class, Integer::valueOf);
        registerConverter(String.class, int.class, Integer::valueOf);
        registerConverter(String.class, Long.class, Long::valueOf);
        registerConverter(String.class, long.class, Long::valueOf);
        registerConverter(String.class, Double.class, Double::valueOf);
        registerConverter(String.class, double.class, Double::valueOf);
        registerConverter(String.class, Float.class, Float::valueOf);
        registerConverter(String.class, float.class, Float::valueOf);
        registerConverter(String.class, Boolean.class, Boolean::valueOf);
        registerConverter(String.class, boolean.class, Boolean::valueOf);
        registerConverter(String.class, Byte.class, Byte::valueOf);
        registerConverter(String.class, byte.class, Byte::valueOf);
        registerConverter(String.class, Short.class, Short::valueOf);
        registerConverter(String.class, short.class, Short::valueOf);
        registerConverter(String.class, Character.class, s -> s.charAt(0));
        registerConverter(String.class, char.class, s -> s.charAt(0));
    }

    private void registerFromBigNumbersToString() {
        registerConverter(BigInteger.class, String.class, BigInteger::toString);
        registerConverter(BigDecimal.class, String.class, BigDecimal::toString);
    }

    private void registerFromStringToBigNumberTypes() {
        registerConverter(String.class, BigInteger.class, BigInteger::new);
        registerConverter(String.class, BigDecimal.class, BigDecimal::new);
    }

    private void registerDateTimeTypesToString() {
        // Date and Time types
        registerConverter(LocalDate.class, String.class, date -> ((LocalDate) date).format(DATE_FORMATTER));
        registerConverter(LocalDateTime.class, String.class, dt -> ((LocalDateTime) dt).format(DATE_TIME_FORMATTER));
        registerConverter(LocalTime.class, String.class, LocalTime::toString);
        registerConverter(ZonedDateTime.class, String.class, ZonedDateTime::toString);
        registerConverter(OffsetDateTime.class, String.class, OffsetDateTime::toString);
        registerConverter(Instant.class, String.class, Instant::toString);
        registerConverter(Year.class, String.class, Year::toString);
        registerConverter(YearMonth.class, String.class, YearMonth::toString);
        registerConverter(MonthDay.class, String.class, MonthDay::toString);
        registerConverter(Date.class, String.class, date -> ((Date) date).toInstant().toString());
    }

    private void registerUtilityTypesToString() {
        registerConverter(UUID.class, String.class, UUID::toString);
        registerConverter(Currency.class, String.class, Currency::getCurrencyCode);
        registerConverter(Locale.class, String.class, Locale::toLanguageTag);
        registerConverter(TimeZone.class, String.class, TimeZone::getID);
    }

    private void registerFromStringToUtilityTypes() {
        // Utility types
        registerConverter(String.class, UUID.class, UUID::fromString);
        registerConverter(String.class, Currency.class, Currency::getInstance);
        registerConverter(String.class, Locale.class, Locale::forLanguageTag);
        registerConverter(String.class, TimeZone.class, TimeZone::getTimeZone);

    }


}