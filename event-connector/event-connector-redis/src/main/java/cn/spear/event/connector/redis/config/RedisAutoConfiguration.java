package cn.spear.event.connector.redis.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/12/12
 */
//@Configuration
public class RedisAutoConfiguration {
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDIS_SSL_PROTOCOL_PREFIX = "rediss://";

    @Bean(destroyMethod = "shutdown")
    @SuppressWarnings("all")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redisson(RedisProperties redisProperties) {
        Duration timeout = redisProperties.getTimeout();
        int timeOutMs = Objects.isNull(timeout) ? 10000 : (int) timeout.toMillis();
        RedissonClient client = useSentinel(redisProperties, timeOutMs);
        if (Objects.isNull(client)) {
            client = useCluster(redisProperties, timeOutMs);
        }
        if (Objects.isNull(client)) {
            client = useSingle(redisProperties, timeOutMs);
        }
        return client;
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(RedissonReactiveClient.class)
    public RedissonReactiveClient redissonReactive(RedissonClient redisson) {
        return redisson.reactive();
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(RedissonRxClient.class)
    public RedissonRxClient redissonRxJava(RedissonClient redisson) {
        return redisson.rxJava();
    }

    private RedissonClient useSentinel(RedisProperties redisProperties, int timeOutMs) {
        RedisProperties.Sentinel sentinelConfig = redisProperties.getSentinel();
        if (Objects.isNull(sentinelConfig) || StrUtil.isBlank(sentinelConfig.getMaster())) {
            return null;
        }
        List<String> nodes = sentinelConfig.getNodes();
        if (CollUtil.isNotEmpty(nodes)) {
            Config config = new Config();
            config.useSentinelServers()
                    .setMasterName(sentinelConfig.getMaster())
                    .addSentinelAddress(nodes.toArray(new String[0]))
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeOutMs)
                    .setUsername(redisProperties.getUsername())
                    .setPassword(redisProperties.getPassword());
            return Redisson.create(config);
        }
        return null;
    }

    private RedissonClient useCluster(RedisProperties redisProperties, int timeOutMs) {
        RedisProperties.Cluster clusterConfig = redisProperties.getCluster();
        if (Objects.isNull(clusterConfig) || CollUtil.isEmpty(clusterConfig.getNodes())) {
            return null;
        }
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(clusterConfig.getNodes().toArray(new String[0]))
                .setConnectTimeout(timeOutMs)
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword());
        return Redisson.create(config);
    }

    private RedissonClient useSingle(RedisProperties redisProperties, int timeOutMs) {
        String prefix = redisProperties.isSsl() ? REDIS_SSL_PROTOCOL_PREFIX : REDIS_PROTOCOL_PREFIX;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setConnectTimeout(timeOutMs)
                .setDatabase(redisProperties.getDatabase())
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword());
        return Redisson.create(config);
    }
}
