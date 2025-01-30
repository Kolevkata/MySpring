package org.example.framework.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.framework.core.IOContainer;
import org.example.framework.core.annotations.Component;
import org.example.framework.core.annotations.Inject;
import org.example.framework.security.annotations.PreAuthorize;
import org.example.framework.security.session.SessionService;
import org.example.framework.security.user.Authority;
import org.example.framework.security.user.UserDetails;
import org.example.framework.util.*;
import org.example.framework.util.type.TypeConverterRegistry;
import org.example.framework.web.annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

@Component
public class DispatcherServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DispatcherServlet.class.getName());
    //request mapping to method
    Set<Endpoint> endpoints = new HashSet<>();
    //method wto controller instance

    @Inject
    SessionService sessionService;
    @Inject
    JSONSerializer jsonSerializer;
    @Inject
    TypeConverterRegistry typeConverterRegistry;

    //todo too long
    @Override
    public void init(ServletConfig config) {
        Map<String, Object> beans = IOContainer.getInstance().getBeans();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            if (!instance.getClass().isAnnotationPresent(Controller.class)) {
                continue;
            }
            for (Method method : instance.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                log.info("Mapping " + annotation.path() + " to " + method.getName());
                Endpoint endpoint = new Endpoint(method, instance.getClass(), annotation.method(), annotation.path());

                if (method.isAnnotationPresent(PreAuthorize.class)) {
                    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                    for (String authority : preAuthorize.allowedAuthorities()) {
                        endpoint.getAllowedAuthorities().add(new Authority(authority));
                    }
                }
                if (endpoints.contains(endpoint)) {
                    log.severe("Duplicate endpoint mapping for " + annotation.path());
                    throw new RuntimeException("Duplicate endpoint mapping for " + annotation.path());
                } else {
                    endpoints.add(endpoint);
                }
            }
        }
    }

    //todo
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getRequestURI();
        RequestType requestType = RequestType.fromString(req.getMethod());
        Optional<Endpoint> endpoint = Endpoint.get(endpoints, requestType, path);


        if (endpoint.isPresent()) {
            try {
                UserDetails user = sessionService.getUserDetailsFromSession(req);
                boolean hasAuthority = false;
                if (user != null) {
                    if (endpoint.get().getAllowedAuthorities().isEmpty()) {
                        hasAuthority = true;
                    } else {
                        for (Authority authority : user.getAuthorities()) {
                            if (endpoint.get().getAllowedAuthorities().contains(authority)) {
                                hasAuthority = true;
                                break;
                            }
                        }
                    }
                    if (!hasAuthority) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }

                IOContainer ioc = IOContainer.getInstance();

                //get the controller instance from ioc container
                Object controller = ioc.getBean(endpoint.get().getController());
                Method method = endpoint.get().getMethod();

                List<Object> params = injectParameters(method, req, resp);

                Object result = method.invoke(controller, params.toArray());

                if (result instanceof ResponseEntity<?> responseEntity) {
                    Object body = responseEntity.getBody();
                    if (body == null) {
                        resp.setStatus(responseEntity.getStatus());
                        return;
                    }
                    Optional<String> converted = typeConverterRegistry.convert(body, String.class);
                    if (converted.isPresent()) {
                        resp.setStatus(responseEntity.getStatus());
                        resp.getWriter().write(converted.get());
                    } else {
                        resp.setContentType("application/json");
                        String json = "";
                        if (body != null) {
                            json = this.jsonSerializer.serialize(body);
                        }
                        resp.setStatus(responseEntity.getStatus());
                        resp.getWriter().write(json);
                    }
                } else if (result instanceof String) {
                    resp.setContentType("text/plain");
                    resp.getWriter().write(result.toString());
                }

            } catch (
                    Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter writer = resp.getWriter();
                writer.write("Error: " + e.getMessage());
            }
        } else {
            resp.

                    setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.

                    getWriter().

                    write("404 Not Found");
        }
    }

    
    private List<Object> injectParameters(Method method, HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        List<Object> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                params.add(resolveRequestBody(param, req));
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                params.add(resolveRequestParam(param, req));
            } else if (param.isAnnotationPresent(PathVariable.class)) {
                params.add(resolverPathVariable(param, req));
            } else if (param.getType().equals(HttpServletRequest.class)) {
                params.add(req);
            } else if (param.getType().equals(HttpServletResponse.class)) {
                params.add(resp);
            } else if (param.getType().equals(UserDetails.class)) {
                params.add(sessionService.getUserDetailsFromSession(req));
            } else {
                IOContainer.getInstance().getBeans().values().stream()
                        .filter(bean -> param.getType().isAssignableFrom(bean.getClass()))
                        .findFirst()
                        .ifPresent(params::add);
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
        String targetPath = Endpoint.get(endpoints, RequestType.fromString(req.getMethod()), req.getRequestURI()).get().getPath();
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
                    Optional<?> o = typeConverterRegistry.convert(element, param.getType());
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
        Optional<?> o = typeConverterRegistry.convert(parameter, param.getType());
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
            String body = getRequestBody(req);
            if (header.equals("application/json")) {
                //arrays and colletions must be handled here because of type amnsia
                Object o;
                if (param.getType().isArray()) {
                    o = handleArray(param, body);
                } else if (Collection.class.isAssignableFrom(param.getType())) {
                    o = handleCollection(param, body);
                } else {
                    o = jsonSerializer.deserialize(body, param.getType());
                }
                return o;
            } else if (param.getType().equals(String.class)) {
                return body;
            }
        } catch (IOException e) {
            log.severe("Encountered error while parsing parameter " + param.getName() + " in met");
            throw new RuntimeException(e);
        }
        return null;
    }

    private Collection<Object> handleCollection(Parameter param, String body) {
        if (param.getParameterizedType() instanceof ParameterizedType parameterizedType) {
            // Check if the parameter is a Collection (or any parameterized type)
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                Type typeArg = typeArgs[0];
                body = StringUtils.removeWhiteSpace(body);
                body = body.substring(1, body.length() - 1);
                String[] subJsons = StringUtils.splitPreservingQuotesAndBrackets(body, ',');
                List<Object> list = new LinkedList<>();

                for (int i = 0; i < subJsons.length; i++) {
                    String s = subJsons[i];
                    Object o = jsonSerializer.deserialize(s, (Class<?>) typeArg);
                    list.add(o);
                }
                return list;
            }
        }
        log.severe("The parameter is NOT a collection.");
        return Collections.emptyList();
    }

    private Object handleArray(Parameter param, String body) {
        Class<?> componentType = param.getType().getComponentType();
        body = StringUtils.removeWhiteSpace(body);
        body = body.substring(1, body.length() - 1);
        String[] subJsons = StringUtils.splitPreservingQuotesAndBrackets(body, ',');
        Object arr = Array.newInstance(componentType, subJsons.length);
        for (int i = 0; i < subJsons.length; i++) {
            String s = subJsons[i];
            Object o = jsonSerializer.deserialize(s, componentType);
            Array.set(arr, i, o);
        }
        return arr;
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