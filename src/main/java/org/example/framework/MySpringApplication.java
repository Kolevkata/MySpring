package org.example.framework;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.example.framework.web.DispatcherServlet;
import org.example.framework.core.IOContainer;

import java.io.File;
import java.util.logging.Logger;

public class MySpringApplication {
    private static final Logger log = Logger.getLogger(MySpringApplication.class.getName());
    private static Tomcat tomcat;
    private static Thread serverThread;

    public static void start() {
        IOContainer ioc = IOContainer.getInstance();
        log.info("IOC container initialized");
        int port = 8080;
        tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(port);
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet());
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
    }

    public static void shutdown() {
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
}