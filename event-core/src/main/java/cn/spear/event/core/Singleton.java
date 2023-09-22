package cn.spear.event.core;

import cn.spear.event.core.lang.Func;
import cn.hutool.core.convert.Convert;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton
 *
 * @author shay
 * @date 2021/3/22
 */
@Slf4j
public final class Singleton {
    private static final ConcurrentHashMap<Class<?>, Object> ALL_SINGLETONS = new ConcurrentHashMap<>();

    private Singleton() {
    }

    public static <T> T instanceFunc(Class<T> clazz, Func<T, Class<T>> instanceFunc) {
        Object obj = ALL_SINGLETONS.computeIfAbsent(clazz, k -> {
            try {
                return null == instanceFunc ? k.newInstance() : instanceFunc.invoke(clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("单例初始化异常", e);
                return null;
            }
        });
        return Convert.convert(clazz, obj);
    }

    public static <T> T instance(Class<T> clazz) {
        Object instance = ALL_SINGLETONS.computeIfAbsent(clazz, k -> {
            try {
                return k.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.warn("create singleton instance error", e);
                return null;
            }
        });
        return clazz.cast(instance);
    }

    public static <T> T instance(T instance) {
        if (instance == null) {
            return null;
        }

        Class<?> clazz = instance.getClass();
        Object instanceObj = ALL_SINGLETONS.computeIfAbsent(clazz, k -> instance);
        return (T) instanceObj;
    }
}
