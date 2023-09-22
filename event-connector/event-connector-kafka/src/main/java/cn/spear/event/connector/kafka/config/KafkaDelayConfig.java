package cn.spear.event.connector.kafka.config;

import cn.spear.event.core.connector.enums.ConsumerResetType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/12/8
 */
@Getter
@Setter
public class KafkaDelayConfig {
    /**
     * 是否开启延迟队列
     */
    private boolean enable = false;
    /**
     * 延迟队列基准 (分钟)
     */
    private int[] basicMinutes = {1, 2, 5, 10, 30, 60};
    /**
     * 延迟队列前缀
     */
    private String topicPrefix = "__event-delay-";
    /**
     * 延迟队列用户
     */
    private String username = "event_delay";
    /**
     * 延迟队列用户密码
     */
    private String password = "event_delay@123";
    private int partitions = 2;
    private int retention = 30;
    private String group = "__event_delay";
    private String clientName = "event-delay";
    private ConsumerResetType resetType = ConsumerResetType.Latest;
    private int maxPollRecords = 200;
    /**
     * 最大暂停时间（ms）
     */
    private long maxPause = 5000;
    /**
     * 延迟阈值（ms,小于等于该值将认为消息到期）
     */
    private long threshold = 100;
}
