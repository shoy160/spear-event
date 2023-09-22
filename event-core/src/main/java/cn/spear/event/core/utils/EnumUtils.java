package cn.spear.event.core.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.core.Constants;
import cn.spear.event.core.enums.*;
import cn.spear.event.core.lang.Func;

import java.util.*;

/**
 * 枚举辅助类
 *
 * @author shay
 * @date 2020/8/8
 */
public final class EnumUtils {

    /**
     * 获取枚举
     *
     * @param value 枚举值
     * @param clazz 枚举类
     * @param <T>   枚举值类型
     * @param <E>   枚举类型
     * @return 枚举
     */
    public static <T, E extends ValueEnum<T>> E getEnum(T value, Class<E> clazz) {
        if (value == null) {
            return null;
        }
        E[] enums = clazz.getEnumConstants();
        for (E e : enums) {
            if (value.equals(e.getValue())) {
                return e;
            }
        }
        return null;
    }

    /**
     * 获取枚举
     *
     * @param enumName 枚举名称
     * @param clazz    枚举类
     * @param <T>      枚举值类型
     * @param <E>      枚举类型
     * @return 枚举
     */
    public static <T, E extends ValueEnum<T>> E getEnum(String enumName, Class<E> clazz) {
        if (StrUtil.isEmpty(enumName)) {
            return null;
        }
        E[] enums = clazz.getEnumConstants();
        for (E e : enums) {
            if (enumName.equals(e.toString())) {
                return e;
            }
        }
        return null;
    }

    /**
     * 获取枚举
     *
     * @param named 枚举名称
     * @param clazz 枚举类
     * @param <T>   枚举值类型
     * @param <E>   枚举类型
     * @return 枚举
     */
    public static <T, E extends ValueNameEnum<T>> E getEnumByNamed(String named, Class<E> clazz) {
        if (StrUtil.isEmpty(named)) {
            return null;
        }
        E[] enums = clazz.getEnumConstants();
        for (E e : enums) {
            if (named.equals(e.getName())) {
                return e;
            }
        }
        return null;
    }

    /**
     * 是否存在枚举值
     *
     * @param value 枚举值
     * @param clazz 枚举类型
     * @param <T>   枚举值
     * @param <E>   枚举
     * @return 是否存在
     */
    public static <T, E extends ValueEnum<T>> boolean isExist(T value, Class<E> clazz) {
        E item = getEnum(value, clazz);
        return item != null;
    }

    /**
     * 获取枚举名称
     *
     * @param value 枚举值
     * @param clazz 枚举类型
     * @param <T>   枚举值
     * @param <E>   枚举
     * @return 枚举名称
     */
    public static <T, E extends ValueNameEnum<T>> String getName(T value, Class<E> clazz) {
        E item = getEnum(value, clazz);
        return item == null ? null : item.getName();
    }

    /**
     * 获取枚举值
     *
     * @param named 枚举名称
     * @param clazz 枚举类型
     * @param <T>   枚举值
     * @param <E>   枚举
     * @return 枚举值
     */
    public static <T, E extends ValueNameEnum<T>> T getValueByNamed(String named, Class<E> clazz) {
        E item = getEnumByNamed(named, clazz);
        return item == null ? null : item.getValue();
    }

    /**
     * 获取位运算标识枚举列表
     *
     * @param value 枚举值
     * @param clazz 枚举类
     * @param <E>   枚举类型
     * @return 枚举
     */
    public static <T extends Number, E extends ValueEnum<T>> E[] getFlags(T value, Class<E> clazz) {
        List<E> list = new ArrayList<>();
        if (value != null) {
            E[] enums = clazz.getEnumConstants();
            for (E e : enums) {
                if (e.getValue() == null) {
                    continue;
                }

                if ((Convert.toLong(value) & Convert.toLong(e.getValue())) > 0) {
                    list.add(e);
                }
            }
        }
        return ArrayUtil.toArray(list, clazz);
    }

    /**
     * 是否有位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    public static <T extends Number, E extends ValueEnum<T>> boolean hasFlag(E value, E flag) {
        if (value == null || flag == null) {
            return false;
        }
        return (Convert.toInt(value.getValue()) & Convert.toInt(flag.getValue())) > 0;
    }

    /**
     * 是否有位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    public static <T extends Number, E extends ValueEnum<T>> boolean hasFlag(T value, E flag) {
        if (value == null || flag == null) {
            return false;
        }
        return (Convert.toLong(value) & Convert.toLong(flag.getValue())) > 0;
    }

    /**
     * 是否同时有位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    @SafeVarargs
    public static <T extends Number, E extends ValueEnum<T>> boolean hasFlags(T value, E... flags) {
        if (value == null) {
            return false;
        }
        if (ArrayUtil.isEmpty(flags)) {
            return true;
        }
        for (E e : flags) {
            if ((Convert.toLong(value) & Convert.toLong(e.getValue())) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否有任意一个位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    @SafeVarargs
    public static <T extends Number, E extends ValueEnum<T>> boolean hasAnyFlags(T value, E... flags) {
        if (value == null) {
            return false;
        }
        if (ArrayUtil.isEmpty(flags)) {
            return true;
        }
        for (E e : flags) {
            if ((Convert.toLong(value) & Convert.toLong(e.getValue())) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    public static <T extends Number, E extends ValueEnum<T>> T addFlag(T value, E flag) {
        if (value == null || flag == null) {
            return value;
        }
        Long result = Convert.toLong(value) | Convert.toLong(flag.getValue());
        return Convert.convert(value.getClass(), result, value);
    }

    /**
     * 添加位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    @SafeVarargs
    public static <T extends Number, E extends ValueEnum<T>> T addFlags(T value, E... flags) {
        if (value == null || ArrayUtil.isEmpty(flags)) {
            return value;
        }
        Long flag = Convert.toLong(value);
        for (E e : flags) {
            flag |= Convert.toLong(e.getValue());
        }
        return Convert.convert(value.getClass(), flag, value);
    }

    /**
     * 移除位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    public static <T extends Number, E extends ValueEnum<T>> T removeFlag(T value, E flag) {
        if (value == null || flag == null) {
            return value;
        }
        long flagValue = Convert.toLong(flag.getValue());
        long result = (Convert.toLong(value) | flagValue) ^ flagValue;
        return Convert.convert(value.getClass(), result, value);
    }

    /**
     * 移除位运算标示
     *
     * @param value 枚举值
     * @param <E>   枚举类型
     * @return 是否有标示
     */
    @SafeVarargs
    public static <T extends Number, E extends ValueEnum<T>> T removeFlags(T value, E... flags) {
        if (value == null || ArrayUtil.isEmpty(flags)) {
            return value;
        }
        Long flag = Convert.toLong(value);
        for (E e : flags) {
            long flagValue = Convert.toLong(e.getValue());
            flag = (flag | flagValue) ^ flagValue;
        }
        return Convert.convert(value.getClass(), flag, value);
    }

    /**
     * 枚举转换
     *
     * @param source   源数据
     * @param tClass   class
     * @param defValue 默认值
     * @param <T>      T
     * @param <V>      V
     * @return 目标美枚举值
     */
    public static <T extends Enum<T>, V extends Enum<V>> T convertByName(V source, Class<T> tClass, T defValue) {
        if (null == source) {
            return defValue;
        }
        for (T item : tClass.getEnumConstants()) {
            if (StrUtil.equalsIgnoreCase(item.name(), source.name())) {
                return item;
            }
        }
        return defValue;
    }

    public static <T extends Enum<T>, V extends Enum<V>> T convertByName(V source, Class<T> tClass) {
        return convertByName(source, tClass, null);
    }

    /**
     * 枚举转换
     *
     * @param source
     * @param tClass
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T extends Enum<T>, V extends Enum<V>> T convertByValue(V source, Class<T> tClass) {
        if (null == source) {
            return null;
        }
        for (T item : tClass.getEnumConstants()) {
            if (NumberUtil.equals(item.ordinal(), source.ordinal())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 枚举转换
     *
     * @param source
     * @param tClass
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T extends BaseEnum, V extends BaseEnum> T convertByValue(V source, Class<T> tClass) {
        if (null == source) {
            return null;
        }
        for (T item : tClass.getEnumConstants()) {
            if (NumberUtil.equals(item.getValue(), source.getValue())) {
                return item;
            }
        }
        return null;
    }

    public static Map<String, Map<Object, String>> findEnums(EnumSerializerType serializerType) {
        return findEnums(Constants.BASE_PACKAGE, serializerType, null);
    }

    public static Map<String, Map<Object, String>> findEnums(
            EnumSerializerType serializerType, Func<Boolean, String> filter
    ) {
        return findEnums(Constants.BASE_PACKAGE, serializerType, filter);
    }

    public static Map<String, Map<Object, String>> findEnums(
            String packageName, EnumSerializerType serializerType, Func<Boolean, String> filter
    ) {
        Set<Class<?>> enumClasses = ReflectUtils.findClasses(packageName, Class::isEnum);
        if (enumClasses.isEmpty()) {
            return new HashMap<>(0);
        }
        Iterator<Class<?>> iterator = enumClasses.iterator();
        Map<String, Map<Object, String>> enumMap = new HashMap<>();
        while (iterator.hasNext()) {
            Class<?> clazz = iterator.next();
            String name = CommonUtils.getName(clazz);
            if (Objects.nonNull(filter) && !filter.invoke(name)) {
                continue;
            }
            Object[] enumConstants = clazz.getEnumConstants();
            boolean isString = EnumSerializerType.String.equals(serializerType);
            Map<Object, String> enumValues = new LinkedHashMap<>();
            Arrays.sort(enumConstants, (a, b) -> {
                if (isString) {
                    return ((Enum<?>) a).name().compareTo(((Enum<?>) b).name());
                }
                return BaseEnum.class.isAssignableFrom(clazz)
                        ? Integer.compare(((BaseEnum) a).getValue(), ((BaseEnum) b).getValue())
                        : Integer.compare(((Enum<?>) a).ordinal(), ((Enum<?>) b).ordinal());
            });
            for (Object item : enumConstants) {
                Object key = item.toString();
                String value = item.toString();
                if (isString) {
                    if (item instanceof BaseNamedEnum) {
                        value = ((BaseNamedEnum) item).getName();
                    }
                } else {
                    if (item instanceof BaseEnum) {
                        key = ((BaseEnum) item).getValue();
                    } else {
                        key = ((Enum<?>) item).ordinal();
                    }
                }
                enumValues.put(key, value);
            }
            enumMap.put(name, enumValues);
        }
        return enumMap;
    }
}
