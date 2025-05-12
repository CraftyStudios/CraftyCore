package dev.crafty.core.storage;

public class StorageUtils {
    public static boolean isPrimitive(Object o) {
        Class<?> c = o.getClass();
        return c.isPrimitive() || c == String.class;
    }
}
