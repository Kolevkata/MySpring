package org.example.framework.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class ReflectionUtils {
    public static Optional<Type> getGenericType(Class<?> clazz) {
        Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterizedType) {
            // Get the actual type arguments
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                return Optional.ofNullable(typeArguments[0]); // The first generic type inside <>
            }
        }
        return Optional.empty();
    }
}
