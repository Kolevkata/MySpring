package org.example.framework.core;

import java.lang.annotation.Annotation;
import java.util.List;

public class AnnotationClassScanner extends ClassScanner {
    private final Class<? extends Annotation> annotation;

    private AnnotationClassScanner(String basePackage, Class<? extends Annotation> annotation) {
        super(basePackage);
        this.annotation = annotation;
    }

    public static List<Class<?>> scan(String basePackage, Class<? extends Annotation> annotation) {
        return new AnnotationClassScanner(basePackage, annotation).scanPackage();
    }

    public static List<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        return scan("org", annotation);
    }

    @Override
    protected boolean shouldIncludeClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(annotation);
    }
}
