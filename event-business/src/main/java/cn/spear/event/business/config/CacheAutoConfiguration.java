package cn.spear.event.business.config;

import cn.spear.event.business.cache.CacheFactory;
import cn.spear.event.business.cache.CacheSyncListener;
import cn.spear.event.business.cache.RedisCache;
import cn.spear.event.business.cache.RedisTemplateFactory;
import cn.spear.event.core.Constants;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.cache.impl.MemoryCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author luoyong
 * @date 2022/11/24
 */
@Configuration
public class CacheAutoConfiguration {
    @Bean
    @SuppressWarnings("all")
    public RedisTemplateFactory redisTemplateFactory(RedisConnectionFactory connectionFactory) {
        return new RedisTemplateFactory(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("all")
    public StringRedisTemplate stringRedisTemplate(RedisTemplateFactory factory) {
        return factory.stringRedisTemplate();
    }

    @Bean
    @Primary
    @SuppressWarnings("all")
    public <T> RedisTemplate<String, T> redisTemplate(RedisTemplateFactory factory) {
        return factory.redisTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("all")
    public RedisMessageListenerContainer container(RedisConnectionFactory factory, CacheSyncListener cacheSyncListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        ChannelTopic topic = new ChannelTopic(Constants.CACHE_SYNC_CHANNEL);
        container.addMessageListener(cacheSyncListener, topic);
        return container;
    }

    @Bean
    public <K, V> MemoryCache<K, V> memoryCache() {
        return new MemoryCache<>();
    }

    @Bean
    @Primary
    @SuppressWarnings("all")
    public <K, V> Cache<K, V> cacheBean(RedisTemplate<String, V> redisTemplate, MemoryCache<K, V> memoryCache) {
        RedisCache<K, V> redisCache = new RedisCache<>(redisTemplate, Constants.APP_NAME);
        return new CacheFactory<>(Constants.APP_NAME, redisCache, memoryCache);
    }
}
