package org.example.framework.core;

import org.example.framework.core.annotations.Component;
import org.example.framework.web.annotations.Controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class MethodScannerAnnotationScanner {
    public static List<Method> scan(Class<? extends Annotation> annotation) {
        return AnnotationClassScanner.scanForAnnotation(Controller.class)
                .stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annotation)))
                .toList();
    }
}