package net.gensokyoreimagined.gensoujank;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionJank {
    public static Iterable<Field> getFields(Class<?> startClass) {

        var fields = new ArrayList<>(List.of(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !parentClass.equals(Object.class)) {
            // lol, recursion
            var parentFields = getFields(parentClass);
            parentFields.forEach(fields::add);
        }

        return fields;
    }

    // i provide a shotgun, you provide your foot
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
