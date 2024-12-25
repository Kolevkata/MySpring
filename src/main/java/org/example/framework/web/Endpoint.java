package org.example.framework.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Endpoint {
    private String path;
    private Method method;
    private Class<?> controller;
    private RequestType requestType;

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
                .filter(endpoint -> endpoint.getPath().equals(path) && endpoint.getRequestType() == requestType)
                .findFirst();
    }
}