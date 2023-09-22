package cn.spear.event.core.cache;

import cn.spear.event.core.lang.Action;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author shay
 * @date 2020/12/15
 */
public interface Cache<K, V> {

    /**
     * 获取缓存区域
     *
     * @return region
     */
    String getRegion();

    /**
     * 默认过期时间
     *
     * @return expiredTime
     */
    long defaultExpired();

    /**
     * 获取缓存
     *
     * @param key key
     * @return cache value
     */
    V get(K key);

    /**
     * 获取缓存
     *
     * @param key        key
     * @param valueFunc  the function to compute a value
     * @param expireDate expiration at date
     * @return cached value
     */
    default V getOrPut(K key, Function<? super K, ? extends V> valueFunc, long expireDate) {
        V value = get(key);
        if (null != value) {
            return value;
        }
        value = valueFunc.apply(key);
        put(key, value, expireDate);
        return value;
    }

    /**
     * 添加缓存
     *
     * @param key        key
     * @param value      value
     * @param expireDate expireDate
     */
    void put(K key, V value, long expireDate);

    /**
     * 删除缓存
     *
     * @param key key
     */
    void remove(K key);

    /**
     * key set
     *
     * @param clazz class
     * @return set
     */
    Set<K> keySet(Class<K> clazz);

    /**
     * 请客所有缓存
     */
    void clean();

    /**
     * 获取缓存信息
     */
    void info();

    /**
     * 缓存键过期事件
     *
     * @param expiredAction action
     * @param clazz         class
     */
    void keyExpired(Action<K> expiredAction, Class<K> clazz);

    /**
     * 获取缓存
     *
     * @param key       key
     * @param valueFunc the function to compute a value
     * @return cached value
     */
    default V getOrPut(K key, Function<? super K, ? extends V> valueFunc) {
        return getOrPut(key, valueFunc, -1L);
    }

    /**
     * 添加缓存
     *
     * @param key   key
     * @param value value
     */
    default void put(K key, V value) {
        put(key, value, -1L);
    }

    /**
     * 获取或添加缓存(随机过期,默认1-10分钟)
     *
     * @param key       key
     * @param valueFunc the function to compute a value
     * @return cached value
     */
    default V randomGetOrPut(K key, Function<? super K, ? extends V> valueFunc) {
        return getOrPut(key, valueFunc, defaultExpired());
    }

    /**
     * 添加缓存(随机过期,默认1-10分钟)
     *
     * @param key   key
     * @param value value
     */
    default void randomPut(K key, V value) {
        put(key, value, defaultExpired());
    }

    /**
     * 添加缓存
     *
     * @param key   key
     * @param value value
     * @param time  expiration time
     * @param unit  unit
     */
    default void put(K key, V value, int time, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(time);
        put(key, value, expireTime);
    }

    /**
     * 添加缓存
     *
     * @param key        key
     * @param value      value
     * @param expireDate expire
     */
    default void put(K key, V value, Date expireDate) {
        put(key, value, expireDate.getTime());
    }


    /**
     * 批量添加缓存
     *
     * @param map  缓存Map
     * @param time expiration time
     * @param unit timeunit
     */
    default void putAll(Map<K, V> map, int time, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(time);
        putAll(map, expireTime);
    }

    /**
     * 批量添加缓存
     *
     * @param map        缓存Map
     * @param expireDate expiration date
     */
    default void putAll(Map<K, V> map, Date expireDate) {
        putAll(map, expireDate.getTime());
    }

    /**
     * 批量添加缓存
     *
     * @param map        缓存Map
     * @param expireDate expiration time
     */
    default void putAll(Map<K, V> map, long expireDate) {
        for (K key : map.keySet()) {
            put(key, map.get(key), expireDate);
        }
    }

    /**
     * 获取/更新缓存
     *
     * @param key       key
     * @param valueFunc the function to compute a value
     * @param time      time
     * @param timeUnit  TimeUnit
     * @return value
     */
    default V getOrPut(K key, Function<? super K, ? extends V> valueFunc, int time, TimeUnit timeUnit) {
        long expiredTime = System.currentTimeMillis() + timeUnit.toMillis(time);
        return getOrPut(key, valueFunc, expiredTime);
    }

    /**
     * 获取/更新缓存
     *
     * @param key            key
     * @param valueFunc      the function to compute a value
     * @param expiredSeconds expiredSeconds
     * @return value
     */
    default V getOrPut(K key, Function<? super K, ? extends V> valueFunc, int expiredSeconds) {
        return getOrPut(key, valueFunc, expiredSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存配置
     *
     * @return setting
     */
    default CacheSetting getSetting() {
        return null;
    }
}
