package org.example.framework.core.annotations;

import org.example.framework.core.ClassScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodScanner extends ClassScanner {
    private final Class<? extends Annotation> annotation;
    private final List<Method> annotatedMethods;

    private MethodScanner(String basePackage, Class<? extends Annotation> annotation) {
        super(basePackage);
        this.annotation = annotation;
        this.annotatedMethods = new ArrayList<>();
    }

    public static List<Method> scan(String basePackage, Class<? extends Annotation> annotation) {
        MethodScanner scanner = new MethodScanner(basePackage, annotation);
        scanner.scanPackage(); // This will populate annotatedMethods
        return scanner.getAnnotatedMethods();
    }

    public static List<Method> scanForAnnotation(Class<? extends Annotation> annotation) {
        return scan("org", annotation);
    }

    @Override
    protected boolean shouldIncludeClass(Class<?> clazz) {
        // Process methods for every class we find
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                annotatedMethods.add(method);
            }
        }
        // We don't need to collect the classes themselves
        return false;
    }

    public List<Method> getAnnotatedMethods() {
        return Collections.unmodifiableList(annotatedMethods);
    }
}