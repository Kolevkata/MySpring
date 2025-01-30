package org.example.framework.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public interface Serializator {
    String serialize(Object obj);

    <T> T deserialize(String serialized, Class<T> targetType);

    /**
     * Synthetic fields are fields that are added by the compiler.
     * We don't want the serializator to try to deserialize these fields.
     */
    default List<Field> getNonSyntheticFields(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .toList();

    }
}
