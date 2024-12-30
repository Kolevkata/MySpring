package org.example.framework.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.framework.annotations.*;
import org.example.framework.core.IOContainer;
import org.example.framework.util.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class DispatcherServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DispatcherServlet.class.getName());
    //request mapping to method
    List<Endpoint> endpoints = new ArrayList<>();
    //method wto controller instance

    @Override
    public void init(ServletConfig config) throws ServletException {
        Map<String, Object> beans = IOContainer.getInstance().getBeans();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            if (instance.getClass().isAnnotationPresent(Controller.class)) {
                for (Method method : instance.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        log.info("Mapping " + annotation.path() + " to " + method.getName());
                        Endpoint endpoint = new Endpoint(method, instance.getClass(), annotation.method(), annotation.path());
                        endpoints.add(endpoint);
                    }
                }
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        RequestType requestType = Mapper.stringToRequestType(req.getMethod());
        Optional<Endpoint> endpoint = Endpoint.get(endpoints, requestType, path);

        if (endpoint.isPresent()) {
            try {
                IOContainer ioc = IOContainer.getInstance();

                //get the controller instance from ioc container
                Object controller = ioc.getBean(endpoint.get().getController());
                Method method = endpoint.get().getMethod();

                List<Object> params = InjectParameters(method, req);

                Object result = method.invoke(controller, params.toArray());

                resp.setContentType("text/plain");
                resp.getWriter().write(result.toString());
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter writer = resp.getWriter();
                writer.write("Error: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
        }
    }

    private List<Object> InjectParameters(Method method, HttpServletRequest req) throws ServletException {
        List<Object> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                params.add(resolveRequestBody(param, req));
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                params.add(resolveRequestParam(param, req));
            } else if (param.isAnnotationPresent(PathVariable.class)) {
                params.add(resolverPathVariable(param, req));
            }
        }
        return params;

    }

    private Object resolverPathVariable(Parameter param, HttpServletRequest req) throws ServletException {
        String pathVarName = param.getAnnotation(PathVariable.class).name();
        if (pathVarName.isEmpty()) {
            pathVarName = param.getName();
        }

        String[] sourcePathSegments = req.getRequestURI().split("/");
        String targetPath = Endpoint.get(endpoints, Mapper.stringToRequestType(req.getMethod()), req.getRequestURI()).get().getPath();
        String[] targetSegments = targetPath.split("/");
        if (sourcePathSegments.length != targetSegments.length) {
            //should never happen but anyways
            throw new ServletException("Cannot resolve path variable, request uri doesnt match");
        }

        for (int i = 0; i < targetSegments.length; i++) {
            String seg = targetSegments[i];
            if (seg.startsWith("{") && seg.endsWith("}")) {
                String currentPathVar = seg.substring(1, seg.length() - 1);
                if (currentPathVar.equals(pathVarName)) {
                    String element = sourcePathSegments[i];
                    Optional<Object> o = Mapper.mapStringToType(element, param.getType());
                    if (o.isEmpty()) {
                        throw new ServletException(String.format("Cannot map path variable %s with value %s to type %s", element, req.getRequestURI(), param.getType().getName()));
                    }
                    return o.get();
                }
            }
        }
        throw new ServletException();

    }

    private Object resolveRequestParam(Parameter param, HttpServletRequest req) throws ServletException {
        String paramName = param.getAnnotation(RequestParam.class).name();
        if (paramName.equals("")) {
            paramName = param.getName();
        }
        String parameter = req.getParameter(paramName);
        Optional<Object> o = Mapper.mapStringToType(parameter, param.getType());
        if (o.isEmpty()) {
            throw new ServletException(String.format("Cannot map req param %s with value %s to type %s", paramName, req.getRequestURI(), param.getType().getName()));
        }
        return o.get();
    }

    private Object resolveRequestBody(Parameter param, HttpServletRequest req) throws ServletException {
        String header = req.getHeader("Content-Type");
        if (header == null) {
            throw new ServletException("Cannot find content type to reseolve request body");
        }
        try {
            Constructor<?> constructor = param.getType().getConstructor();
            constructor.setAccessible(true);
            Object object = constructor.newInstance();
            String body = getRequestBody(req);
            if (header.equals("application/json")) {
                Object o = JSON.fromJson(body, param.getType());
                return o;
            } else if (param.getType().equals(String.class)) {
                return body;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 IOException e) {
            log.severe("Encountered error while parsing parameter " + param.getName() + " in met");
            throw new RuntimeException(e);
        }
        return null;
    }

    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        return buffer.toString();
    }
}