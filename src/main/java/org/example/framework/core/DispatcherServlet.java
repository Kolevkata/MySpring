package org.example.framework.core;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.framework.annotations.Controller;
import org.example.framework.annotations.RequestMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DispatcherServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DispatcherServlet.class.getName());
    //request mapping to method
    private final Map<String, Method> handlerMapping = new HashMap<>();
    //method wto controller instance
    private final Map<Method, Object> controllerMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        Map<String, Object> beans = IOContainer.getInstance().getBeans();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            if (instance.getClass().isAnnotationPresent(Controller.class)) {
                for (Method method : instance.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        String path = method.getAnnotation(RequestMapping.class).value();
                        log.info("Mapping " + path + " to " + method.getName());
                        handlerMapping.put(path, method);
                        controllerMapping.put(method, instance);
                    }
                }
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        Method handler = handlerMapping.get(path);

        if (handler != null) {
            try {
                Object controller = controllerMapping.get(handler);
                Object result = handler.invoke(controller);

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
