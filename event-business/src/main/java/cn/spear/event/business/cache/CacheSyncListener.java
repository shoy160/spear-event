package cn.spear.event.business.cache;

import cn.spear.event.core.cache.SyncCacheKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author shoy
 * @date 2022/9/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSyncListener implements MessageListener {
    private CacheFactory<String, Object> cacheFactory;

    @Autowired(required = false)
    public void setCacheFactory(CacheFactory<String, Object> cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        if (null == cacheFactory) {
            return;
        }
        Object value = RedisSerializer.json().deserialize(message.getBody());
        if (!(value instanceof SyncCacheKey)) {
            return;
        }
        SyncCacheKey syncKey = (SyncCacheKey) value;
        log.debug("redis receive sync message -> {},{},{}", syncKey.getFactoryId(), syncKey.getRegion(), syncKey.getKey());
        if (Objects.equals(syncKey.getFactoryId(), cacheFactory.getFactoryId())) {
            return;
        }
        log.debug("clean memory cache - > {}", syncKey.getKey());
        cacheFactory.cleanMemory(syncKey.getKey());
    }
}
