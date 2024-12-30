package org.example.framework.util;

import org.example.framework.web.RequestType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class Mapper {
    public static RequestType stringToRequestType(String method) {
        return switch (method) {
            case "GET" -> RequestType.GET;
            case "POST" -> RequestType.POST;
            case "OPTIONS" -> RequestType.OPTIONS;
            case "DELETE" -> RequestType.DELETE;
            case "PATCH" -> RequestType.PATCH;
            default -> throw new IllegalArgumentException(String.format("Invalid request method type: ", method));
        };

    }

    public static <T> Optional<T> mapStringToType(String element, Class<?> target) {
        if (target == String.class) {
            return Optional.of((T) element.replaceAll("\"", "")); // Remove quotes from String
        } else if (target == Integer.class || target == int.class) {
            return Optional.of((T) Integer.valueOf(element)); // Parse Integer
        } else if (target == Long.class || target == long.class) {
            return Optional.of((T) Long.valueOf(element)); // Parse Integer
        } else if (target == Double.class || target == double.class) {
            return Optional.of((T) Double.valueOf(element)); // Parse Double
        } else if (target == BigInteger.class) {
            return Optional.of((T) BigInteger.valueOf(Long.valueOf(element))); // Parse BigInt
        } else if (target == BigDecimal.class) {
            return Optional.of((T) BigDecimal.valueOf(Double.valueOf(element))); // Parse BigDecimal
        } else if (target == Boolean.class || target == boolean.class) {
            return Optional.of((T) Boolean.valueOf(element)); // Parse Boolean
        } else if (target == LocalDate.class) {
            return Optional.of((T) parseLocalDate(element)); // Parse LocalDate
        } else if (target == LocalDateTime.class) {
            return Optional.of((T) parseLocalDateTime(element)); // Parse LocalDateTime
        } else if (target == Date.class) {
            return Optional.of((T) parseDate(element)); // Parse LocalDateTime
        }
        return Optional.empty();
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

}
