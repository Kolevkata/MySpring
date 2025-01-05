package org.example.framework;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.example.framework.core.annotations.MethodScanner;
import org.example.framework.core.annotations.OnDestroy;
import org.example.framework.core.annotations.OnInit;
import org.example.framework.web.DispatcherServlet;
import org.example.framework.core.IOContainer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

public class MySpringApplication {
    private static final Logger log = Logger.getLogger(MySpringApplication.class.getName());
    private static Tomcat tomcat;
    private static Thread serverThread;

    public static void start() {

        IOContainer ioc = IOContainer.getInstance();
        log.info("IOC container initialized");
        //run Lifecycle beans
        List<Method> onInitMethods = MethodScanner.scan("org", OnInit.class);
        runMethods(onInitMethods);

        //tomcat
        int port = 8080;
        tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(port);
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);
        DispatcherServlet dispatcherServlet = (DispatcherServlet) ioc.getBean(DispatcherServlet.class);
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        wrapper.setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcher");

        tomcat.getConnector();

        try {
            tomcat.start();
            log.info("Tomcat started successfully on port " + port);
            serverThread = new Thread(() -> tomcat.getServer().await());
            serverThread.setDaemon(true);
            serverThread.start();
        } catch (LifecycleException e) {
            log.severe("Failed to start Tomcat");
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            shutdown();
        }));
    }

    public static void shutdown() {
        //run shutdown methods
        List<Method> onDestroyMethods = MethodScanner.scan("org", OnDestroy.class);
        runMethods(onDestroyMethods);

        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
                if (serverThread != null) {
                    serverThread.interrupt();
                }
                log.info("Tomcat shutdown completed");
            } catch (LifecycleException e) {
                log.severe("Failed to shutdown Tomcat");
                e.printStackTrace();
            }
        }
    }

    private static void runMethods(List<Method> methods) {
        for (Method method : methods) {
            try {
                IOContainer container = IOContainer.getInstance();
                Object bean = container.getBean(method.getDeclaringClass());
                method.invoke(bean);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.severe("Cannot execute method " + method.getName());
                throw new RuntimeException(e);
            }
        }
    }
}