package org.example.framework.web;

public enum RequestType {
    GET, POST, PUT, PATCH, DELETE, OPTIONS;

    public static RequestType fromString(String method) throws IllegalArgumentException {
        return switch (method) {
            case "GET" -> RequestType.GET;
            case "POST" -> RequestType.POST;
            case "OPTIONS" -> RequestType.OPTIONS;
            case "DELETE" -> RequestType.DELETE;
            case "PATCH" -> RequestType.PATCH;
            default -> throw new IllegalArgumentException(String.format("Invalid request method type: %s", method));
        };

    }
}
