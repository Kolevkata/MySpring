package org.example.framework.util;

import org.example.framework.web.RequestType;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
