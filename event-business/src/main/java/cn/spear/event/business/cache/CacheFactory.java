package cn.spear.event.business.cache;

import cn.spear.event.core.cache.BaseCache;
import cn.spear.event.core.cache.SyncCacheKey;
import cn.spear.event.core.cache.impl.MemoryCache;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author shoy
 * @date 2022/9/30
 */
@Slf4j
public class CacheFactory<K, V> extends BaseCache<K, V> {
    private final String factoryId;
    private final MemoryCache<K, V> memoryCache;
    private final RedisCache<K, V> redisCache;

    public CacheFactory(String region, RedisCache<K, V> secondCache, MemoryCache<K, V> memoryCache) {
        super(region);
        this.factoryId = IdUtil.fastSimpleUUID();
        this.memoryCache = memoryCache;
        this.redisCache = secondCache;
    }

    public String getFactoryId() {
        return this.factoryId;
    }

    @Override
    public V get(K key) {
        V value = memoryCache.get(key);
        if (null != value) {
            log.debug("get cache value from memory -> {}", key);
            return value;
        }
        if (null != redisCache) {
            value = redisCache.get(key);
            if (null != value) {
                log.debug("get cache from redis -> {}", key);
                memoryCache.put(key, value);
                return value;
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value, long expireDate) {
        if (null != redisCache) {
            memoryCache.put(key, value);
            redisCache.put(key, value, expireDate);
            sendSync(key);
        } else {
            memoryCache.put(key, value, expireDate);
        }
    }

    @Override
    public void remove(K key) {
        memoryCache.remove(key);
        if (null != redisCache) {
            redisCache.remove(key);
            sendSync(key);
        }
    }

    @Override
    public Set<K> keySet(Class<K> clazz) {
        return null;
    }

    @Override
    public void clean() {

    }

    public void cleanMemory(K key) {
        memoryCache.remove(key);
    }

    private void sendSync(K key) {
        if (null == redisCache) {
            return;
        }
        SyncCacheKey cacheKey = new SyncCacheKey(getRegion(), stringKey(key, false), this.factoryId);
        this.redisCache.sendSync(cacheKey);
    }
}
