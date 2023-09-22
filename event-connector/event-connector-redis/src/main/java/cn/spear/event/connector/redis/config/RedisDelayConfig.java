package cn.spear.event.connector.redis.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/12/8
 */
@Getter
@Setter
public class RedisDelayConfig {
    private boolean enable = false;
    private int maxThreads = 1;
    /**
     * 延迟 Topic
     */
    private String delayTopic = "__event_redis_delay";
    /**
     * 延迟队列用户
     */
    private String username = "event_redis_delay";
    /**
     * 延迟队列用户密码
     */
    private String password = "event_delay@redis";
    private String consumerGroup = "__event_redis_delay";
    private String delayQueueKey = "authing-event:delay:events";
    private int maxPollRecords = 200;
    private int partitions = 2;
    private int replications = 1;
    private int retention = 30;
    private int initialDelay = 0;
    /**
     * 循环速率(ms)
     */
    private int fixedRate = 1000;
    /**
     * 获取数据时间精度(ms)
     */
    private int timeAccuracy = 1000;
    /**
     * Redis 单次获取最大数量
     */
    private int maxFetchCount = 100;
}
