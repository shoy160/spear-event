package cn.spear.event.core.cache.impl;

import cn.spear.event.core.Constants;
import cn.spear.event.core.Singleton;
import cn.spear.event.core.cache.BaseCache;
import cn.spear.event.core.cache.CacheItem;
import cn.spear.event.core.lang.Action;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author shay
 * @date 2020/12/15
 */
@Slf4j
public class MemoryCache<K, V> extends BaseCache<K, V> {
    private final ConcurrentHashMap<K, CacheItem<V>> mapCache;

    private Action<K> expiredListener;

    public MemoryCache() {
        this(Constants.STR_EMPTY, 0, 200);
    }

    public MemoryCache(String region) {
        this(region, 0, 200);
    }

    public MemoryCache(String region, int cleanInterval) {
        this(region, 0, cleanInterval);
    }


    public MemoryCache(String region, int cleanDelay, int cleanInterval) {
        super(region);
        log.info("init map cache:{}", region);
        this.mapCache = new ConcurrentHashMap<>();
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().setNamePrefix("memory-cache-").build();
        ScheduledExecutorService swapExpiredPool = new ScheduledThreadPoolExecutor(1, threadFactory);
        swapExpiredPool.scheduleAtFixedRate(this::cleanExpired, cleanDelay, cleanInterval, TimeUnit.MILLISECONDS);
    }

    public static <TK, TV> MemoryCache<TK, TV> getInstance(String region) {
        return Singleton.instance(new MemoryCache<>(region));
    }

    @Override
    public void put(K key, V value, long expireDate) {
        CacheItem<V> item = new CacheItem<>(value, expireDate);
        this.mapCache.put(key, item);
    }

    @Override
    public void put(K key, V value) {
        this.put(key, value, defaultExpired());
    }

    @Override
    public V get(K key) {
        if (this.mapCache.containsKey(key)) {
            return this.mapCache.get(key).getValue();
        }
        return null;
    }

    @Override
    public V getOrPut(K key, Function<? super K, ? extends V> valueFunc, long expireDate) {
        if (this.mapCache.containsKey(key)) {
            return this.mapCache.get(key).getValue();
        }
        V value = valueFunc.apply(key);
        put(key, value, expireDate);
        return value;
    }

    @Override
    public void remove(K key) {
        this.mapCache.remove(key);
    }

    @Override
    public Set<K> keySet(Class<K> clazz) {
        return this.mapCache.keySet();
    }

    @Override
    public void clean() {
        this.mapCache.clear();
    }

    @Override
    public void info() {
        log.info("cache count:{}", this.mapCache.size());
    }

    @Override
    public void keyExpired(Action<K> expiredAction, Class<K> clazz) {
        this.expiredListener = expiredAction;
    }

    /**
     * 清空已过期缓存
     */
    private void cleanExpired() {
        for (K key : this.mapCache.keySet()) {
            CacheItem<V> item = this.mapCache.get(key);
            long expireTime = item.getExpireTime();
            // <= 0 无过期时间
            if (expireTime <= 0 || expireTime > System.currentTimeMillis()) {
                continue;
            }
            log.info("clean key:{},hit:{}", key, item.getHitCount());
            this.mapCache.remove(key);
            if (this.expiredListener != null) {
                this.expiredListener.invoke(key);
            }
        }
    }
}
