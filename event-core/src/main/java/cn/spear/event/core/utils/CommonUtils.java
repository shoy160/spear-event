package cn.spear.event.core.utils;

import cn.spear.event.core.annotation.Naming;
import cn.spear.event.core.enums.ValueEnum;
import cn.spear.event.core.lang.Action;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 通用辅助类
 *
 * @author shay
 * @date 2020/7/15
 */
public final class CommonUtils {
    public static boolean isEmpty(Object value) {
        if (null == value) {
            return true;
        }

        return ObjectUtil.isEmpty(value);
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    public static String unEscape(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ValueEnum) {
            return ((ValueEnum<?>) value).getValue().toString();
        }
        return value.toString();
    }

    public static boolean isJwt(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        String[] array = token.split("\\.");
        return 3 == array.length;
    }

    public static <T> T toBean(Object source, Class<T> clazz) {
        if (null == source) {
            return null;
        }
        CopyOptions copyOptions = CopyOptions.create().ignoreError();
        return BeanUtil.toBean(source, clazz, copyOptions);
    }

    /**
     * 获取映射关系
     *
     * @param clazz   class
     * @param reverse 是否反序列化
     * @return map
     */
    public static Map<String, String> getFieldMapping(Class<?> clazz, boolean reverse) {
        Map<String, String> mapping = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String name = getName(field);
            if (!field.getName().equals(name)) {
                if (reverse) {
                    //反序列化
                    mapping.put(name, field.getName());
                } else {
                    //序列化
                    mapping.put(field.getName(), name);
                }
            }
        }
        return mapping;
    }

    public static <T> T toBean(Object source, Class<T> clazz, CopyOptions copyOptions) {
        if (null == source) {
            return null;
        }
        return BeanUtil.toBean(source, clazz, copyOptions);
    }

    public static <T, TS> List<T> toListBean(List<TS> sourceList, Class<T> clazz) {
        if (ObjectUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (TS item : sourceList) {
            list.add(toBean(item, clazz));
        }
        return list;
    }

    public static <T, TS> List<T> toListBean(List<TS> sourceList, Class<T> clazz, CopyOptions copyOptions) {
        return toListBean(sourceList, clazz, copyOptions, null);
    }

    public static <T, TS> List<T> toListBean(List<TS> sourceList, Class<T> clazz, Action<T> convertAction) {
        return toListBean(sourceList, clazz, null, convertAction);
    }

    public static <T, TS> List<T> toListBean(List<TS> sourceList, Class<T> clazz, CopyOptions copyOptions, Action<T> convertAction) {
        if (ObjectUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (TS source : sourceList) {
            T item = toBean(source, clazz, copyOptions);
            if (item == null) {
                continue;
            }
            if (convertAction != null) {
                convertAction.invoke(item);
            }
            list.add(item);
        }
        return list;
    }

    public static String getName(Class<?> clazz) {
        Naming naming = AnnotationUtil.getAnnotation(clazz, Naming.class);
        String name = clazz.getSimpleName();
        if (naming != null && CommonUtils.isNotEmpty(naming.name())) {
            name = naming.name();
        }
        return getNaming(name, naming);
    }

    public static String getName(Field field) {
        return getNaming(field, field.getDeclaringClass(), false);
    }

    public static String getName(Field field, Class<?> clazz) {
        return getNaming(field, clazz, false);
    }

    public static String getDesc(Field field) {
        return getNaming(field, null, true);
    }

    public static String getDesc(Field field, Class<?> clazz) {
        return getNaming(field, clazz, true);
    }

    private static String getNaming(String name, Naming naming) {
        if (naming == null) {
            return name;
        }
        switch (naming.type()) {
            case CamelCase:
                name = StrUtil.toCamelCase(name);
                break;
            case SnakeCase:
                name = StrUtil.toUnderlineCase(name);
                break;
            case UpperCase:
                name = name.toUpperCase();
                break;
            default:
                break;
        }
        return naming.prefix().concat(name).concat(naming.suffix());
    }

    private static String getNaming(Field field, Class<?> clazz, boolean desc) {
        Naming naming = AnnotationUtil.getAnnotation(field, Naming.class);
        boolean fieldNaming = true;
        if (naming == null) {
            fieldNaming = false;
            if (clazz == null) {
                clazz = field.getDeclaringClass();
            }
            naming = AnnotationUtil.getAnnotation(clazz, Naming.class);
        }
        if (naming == null) {
            return field.getName();
        }
        if (fieldNaming) {
            if (desc && !StrUtil.isEmpty(naming.desc())) {
                return naming.desc();
            } else if (!StrUtil.isEmpty(naming.name())) {
                return naming.name();
            }
        }
        String name = field.getName();
        return getNaming(name, naming);
    }
}
