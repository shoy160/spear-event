package cn.spear.event.core.utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 * Type Utils
 *
 * @author shay
 * @date 2020/9/17
 */
public class TypeUtils {
    public static <T> boolean isString(T t) {
        return t instanceof String;
    }

    public static boolean isString(Class<?> clazz) {
        return CharSequence.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz);
    }

    public static <T> boolean isByte(T t) {
        return t instanceof Byte;
    }

    public static boolean isByte(Class<?> clazz) {
        return Byte.class.equals(clazz);
    }

    public static <T> boolean isShort(T t) {
        return t instanceof Short;
    }

    public static boolean isShort(Class<?> clazz) {
        return Short.class.equals(clazz);
    }

    public static <T> boolean isInt(T t) {
        return t instanceof Integer;
    }

    public static boolean isInt(Class<?> clazz) {
        return Integer.class.equals(clazz);
    }

    public static <T> boolean isLong(T t) {
        return t instanceof Long;
    }

    public static boolean isLong(Class<?> clazz) {
        return Long.class.equals(clazz);
    }

    public static <T> boolean isChar(T t) {
        return t instanceof Character;
    }

    public static boolean isChar(Class<?> clazz) {
        return Character.class.equals(clazz);
    }

    public static <T> boolean isFloat(T t) {
        return t instanceof Float;
    }

    public static boolean isFloat(Class<?> clazz) {
        return Float.class.equals(clazz);
    }

    public static <T> boolean isDouble(T t) {
        return t instanceof Double;
    }

    public static boolean isDouble(Class<?> clazz) {
        return Double.class.equals(clazz);
    }

    public static <T> boolean isBytes(T t) {
        return t instanceof Byte;
    }

    public static boolean isBytes(Class<?> clazz) {
        return Byte.class.equals(clazz);
    }

    public static <T> boolean isDate(T t) {
        return t instanceof Date;
    }

    public static boolean isDate(Class<?> clazz) {
        return Date.class.isAssignableFrom(clazz) || LocalDateTime.class.isAssignableFrom(clazz);
    }

    /**
     * 是否是简单类型
     *
     * @param t   instance
     * @param <T> T
     * @return boolean
     */
    public static <T> boolean isSimple(T t) {
        return isString(t) || isInt(t) || isByte(t) || isShort(t)
                || isLong(t) || isChar(t) || isFloat(t)
                || isDouble(t) || isDate(t) || isBytes(t);
    }

    /**
     * 是否是简单类型
     *
     * @param clazz class
     * @return boolean
     */
    public static boolean isSimple(Class<?> clazz) {
        return isString(clazz) || isInt(clazz) || isByte(clazz)
                || isShort(clazz) || isLong(clazz) || isChar(clazz)
                || isFloat(clazz) || isDouble(clazz) || isDate(clazz) || isBytes(clazz);
    }

    public static <T> Class<?> getClassType(T t) {
        return t.getClass();
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isArray(Object value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return isArray(value.getClass());
    }

    public static boolean isNumber(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }

    public static boolean isBoolean(Class<?> clazz) {
        return clazz.equals(Boolean.class);
    }
}
