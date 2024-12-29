package org.example.framework.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Endpoint {
    private final String path;
    private final Method method;
    private final Class<?> controller;
    private final RequestType requestType;

    public Endpoint(Method method, Class<?> controllerClass, RequestType requestType, String path) {
        this.method = method;
        this.requestType = requestType;
        this.path = path;
        this.controller = controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getController() {
        return controller;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getPath() {
        return path;
    }

    public static Optional<Endpoint> get(List<Endpoint> endpoints, RequestType requestType, String path) {
        return endpoints.stream()
                .filter(endpoint -> equalsIgnoreInsideBrackets(endpoint.getPath(),path) && endpoint.getRequestType() == requestType)
                .findFirst();
    }

    private static boolean equalsIgnoreInsideBrackets(String p1, String p2) {
        if (p1 == null || p2 == null) {
            return false;
        }

        // Split both paths into their segments
        String[] p1Segments = p1.split("/");
        String[] p2Segments = p2.split("/");

        // If the number of segments is different, the paths cannot match
        if (p1Segments.length != p2Segments.length) {
            return false;
        }

        // Compare each segment
        for (int i = 0; i < p1Segments.length; i++) {
            String segment1 = p1Segments[i];
            String segment2 = p2Segments[i];

            // Ignore segments in p1 that are placeholders (e.g., "{variable}")
            if (segment1.startsWith("{") && segment1.endsWith("}")) {
                continue; // Skip comparison for placeholders
            }

            // Compare fixed segments
            if (!segment1.equals(segment2)) {
                return false;
            }
        }

        // If all segments match or placeholders align, the paths are equal
        return true;
    }
}