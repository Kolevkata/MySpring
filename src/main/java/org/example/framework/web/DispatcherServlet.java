package org.example.framework.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.framework.annotations.Controller;
import org.example.framework.annotations.RequestMapping;
import org.example.framework.core.IOContainer;
import org.example.framework.util.Mapper;

import java.io.IOException;
import java.lang.reflect.Method;
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
                Object result = method.invoke(controller);

                resp.setContentType("text/plain");
                resp.getWriter().write(result.toString());
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Error: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
        }
    }
}