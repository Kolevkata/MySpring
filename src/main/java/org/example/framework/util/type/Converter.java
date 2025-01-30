package org.example.framework.util.type;

import java.util.Objects;
import java.util.function.Function;

class Converter<T, S> {
    private Class<T> sourceType;
    private Class<S> targetType;
    private Function<T, S> converterFunction;

    public Converter(Class<T> sourceType, Class<S> targetType, Function<T, S> converterFunction) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.converterFunction = converterFunction;
    }

    public S convert(T source) {
        return converterFunction.apply(source);
    }

    public Class<T> getSourceType() {
        return sourceType;
    }

    public Class<S> getTargetType() {
        return targetType;
    }
    @Override
    public boolean equals(Object o) {
        if (isNotNullAndSameType(o)) {
            return false;
        }
        Converter<?, ?> converter = (Converter<?, ?>) o;
        return converter.sourceType == sourceType && converter.targetType == targetType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType);
    }

    private boolean isNotNullAndSameType(Object o) {
        return o != null && getClass() != o.getClass();
    }

}