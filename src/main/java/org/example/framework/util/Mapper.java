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

}
