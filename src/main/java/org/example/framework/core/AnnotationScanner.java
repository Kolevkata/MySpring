package org.example.framework.core;

import org.example.framework.MySpringApplication;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AnnotationScanner {
    private static final Logger log = Logger.getLogger(AnnotationScanner.class.getName());

    private static List<Class<?>> scanForAnnotation(String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        String path = packageName.replace(".", "/");
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }

        File directory = new File(resource.getFile());
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    annotatedClasses.addAll(scanForAnnotation(packageName + "." + file.getName(), annotation));
                } else {
                    try {
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(packageName + "." + className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            annotatedClasses.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            log.severe("dir " + directory.getName() + " not found");
        }
        return annotatedClasses;
    }


    public static List<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        return scanForAnnotation("org", annotation);
    }
}
