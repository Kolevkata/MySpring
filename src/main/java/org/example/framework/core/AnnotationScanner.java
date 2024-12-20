package org.example.framework.core;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnnotationScanner {
    private static List<Class<?>> scanForAnnotation(String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        String path = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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
        }
        return annotatedClasses;
    }


    public static List<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        return scanForAnnotation("org", annotation);
    }
}
