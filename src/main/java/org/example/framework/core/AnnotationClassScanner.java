package org.example.framework.core;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class AnnotationScanner extends ClassScanner {
    private final Class<? extends Annotation> annotation;

    private AnnotationScanner(String basePackage, Class<? extends Annotation> annotation) {
        super(basePackage);
        this.annotation = annotation;
    }

    public static List<Class<?>> scan(String basePackage, Class<? extends Annotation> annotation) {
        return new AnnotationScanner(basePackage, annotation).scanPackage();
    }

    public static List<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        return scan("org", annotation);
    }

    @Override
    protected boolean shouldIncludeClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(annotation);
    }
}
