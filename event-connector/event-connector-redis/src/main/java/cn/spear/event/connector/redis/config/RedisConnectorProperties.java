package cn.spear.event.connector.redis.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author luoyong
 * @date 2022/12/8
 */
@Getter
@Setter
@Slf4j
@Component
@ConfigurationProperties(prefix = "spear.connector.redis")
public class RedisConnectorProperties {
    /**
     * 延迟配置
     */
    @NestedConfigurationProperty
    private RedisDelayConfig delay = new RedisDelayConfig();
}
