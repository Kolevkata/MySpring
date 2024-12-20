package org.example.framework;

import org.example.framework.annotations.Component;
import org.example.framework.core.AnnotationScanner;

import java.util.logging.Logger;

public class MySpringApplication {
    private static final Logger log = Logger.getLogger(MySpringApplication.class.getName());

    public static void start() {
        log.info("Starting application");
        AnnotationScanner.scanForAnnotation(Component.class)
                .forEach(clazz ->
                        log.info(String.format("Found component: %s", clazz.getName())));
    }
}