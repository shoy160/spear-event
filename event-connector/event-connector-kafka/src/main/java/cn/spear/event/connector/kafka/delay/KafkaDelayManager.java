package cn.spear.event.connector.kafka.delay;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.connector.kafka.config.KafkaConnectorProperties;
import cn.spear.event.connector.kafka.config.KafkaDelayConfig;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.DelayManager;
import cn.spear.event.core.connector.Producer;
import cn.spear.event.core.connector.enums.EventGrantType;
import cn.spear.event.core.connector.enums.EventPatternType;
import cn.spear.event.core.connector.model.EventAclBinding;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luoyong
 * @date 2022/11/30
 */
@Slf4j
@Component
public class KafkaDelayManager implements DelayManager {
    private final Map<String, Integer> BASIC_DELAY_MAP = new LinkedHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final KafkaConnectorProperties config;
    private final KafkaDelayConfig delayConfig;
    private final ConnectorAcl connectorAcl;
    private final ConnectorManager connectorManager;
    private ExecutorService executorService;

    public KafkaDelayManager(KafkaConnectorProperties config, ConnectorAcl connectorAcl, ConnectorManager connectorManager) {
        this.config = config;
        this.delayConfig = config.getDelay();
        this.connectorAcl = connectorAcl;
        this.connectorManager = connectorManager;
        if (delayConfig.isEnable()) {
            initDelayConfig(this.delayConfig.getBasicMinutes());
            this.start();
        } else {
            log.info("Kafka 延迟队列未启用");
        }
    }

    private void initDelayConfig(int[] delayMinutes) {
        int[] minutes = Arrays.stream(delayMinutes)
                .distinct()
                .sorted()
                .toArray();
        log.info("init basic delay topics:{}", minutes);
        String delayTopicPrefix = this.delayConfig.getTopicPrefix();
        if (this.config.isEnableAcl()) {
            String delayGroup = this.delayConfig.getGroup();
            String username = this.delayConfig.getUsername();
            boolean createResult = this.connectorAcl.createUser(username, this.delayConfig.getPassword());
            if (createResult) {
                EventAclBinding aclBinding = new EventAclBinding();
                aclBinding.setUser(username);
                aclBinding.setGrantType(EventGrantType.ProducerAndConsumer);
                aclBinding.setPatternType(EventPatternType.Prefixed);
                aclBinding.setTopic(delayTopicPrefix);
                aclBinding.setGroup(delayGroup);
                this.connectorAcl.addAcl(aclBinding);
            }
        }
        for (int i = minutes.length - 1; i >= 0; i--) {
            String topic = String.format("%s%dm", delayTopicPrefix, minutes[i]);
            this.connectorAcl.createTopic(
                    topic, this.delayConfig.getPartitions(), null,
                    this.delayConfig.getRetention()
            );
            BASIC_DELAY_MAP.put(topic, minutes[i]);
        }
    }

    @Override
    public void sendDelay(@NonNull CloudEvent cloudEvent, int delayMinutes) {
        String topic = cloudEvent.getSubject();
        if (StrUtil.isBlank(topic)) {
            throw new ConnectorRuntimeException("Topic 不能为空");
        }
        OffsetDateTime eventTime = Optional.ofNullable(cloudEvent.getTime()).orElse(OffsetDateTime.now());
        long sendTime = eventTime.toInstant().toEpochMilli() + delayMinutes * 60 * 1000L;
        CloudEvent event = new CloudEventBuilder(cloudEvent)
                .withExtension(Constants.ATTR_DELAY_TARGET_TOPIC, topic)
                .withExtension(Constants.ATTR_DELAY_MINUTES, delayMinutes)
                .withExtension(Constants.ATTR_DELAY_SEND_TIME, sendTime)
                .build();
        sendEvent(event);
    }

    @Override
    public void sendDelay(@NonNull CloudEvent cloudEvent, Date delayAt) {
        throw new UnsupportedOperationException("不支持定时发送");
    }

    private void sendEvent(CloudEvent cloudEvent) {
        long leftTime = Convert.toLong(cloudEvent.getExtension(Constants.ATTR_DELAY_MINUTES), 0L);
        if (leftTime <= 0) {
            // 业务 Topic
            if (log.isDebugEnabled()) {
                log.debug("send event to business:{}", cloudEvent.getId());
            }
            Object targetTopic = cloudEvent.getExtension(Constants.ATTR_DELAY_TARGET_TOPIC);
            if (null != targetTopic) {
                cloudEvent = new CloudEventBuilder(cloudEvent)
                        .withSubject(targetTopic.toString())
                        .withoutExtension(Constants.ATTR_DELAY_TARGET_TOPIC)
                        .withoutExtension(Constants.ATTR_DELAY_MINUTES)
                        .withoutExtension(Constants.ATTR_DELAY_SEND_TIME)
                        .build();
            }
            //todo 拿客户端信息
            Producer producer = connectorManager.createEventProducer();
            producer.sendOneway(cloudEvent);
        } else {
            //  剩余时间计算
            String nextTopic = "";
            int time = 0;
            for (Map.Entry<String, Integer> entry : BASIC_DELAY_MAP.entrySet()) {
                if (leftTime >= entry.getValue()) {
                    nextTopic = entry.getKey();
                    time = entry.getValue();
                    break;
                }
            }
            CloudEvent event = new CloudEventBuilder(cloudEvent)
                    .withExtension(Constants.ATTR_DELAY_MINUTES, leftTime - time)
                    .withSubject(nextTopic)
                    .build();
            if (log.isDebugEnabled()) {
                log.debug("send event to next delay:{} -> {}, left:{}", cloudEvent.getId(), time, leftTime - time);
            }
            Producer producer = connectorManager.createProducer(this.delayConfig.getUsername(), this.delayConfig.getPassword());
            producer.sendOneway(event);
        }
    }

    @Override
    public boolean isStarted() {
        return this.started.get();
    }

    @Override
    public boolean isClosed() {
        return !this.isStarted();
    }

    @Override
    public void start() {
        if (this.started.compareAndSet(false, true)) {
            if (null == this.executorService || this.executorService.isShutdown()) {
                this.executorService =
                        ThreadUtil.newFixedExecutor(BASIC_DELAY_MAP.size(), "k-delay", true);
            }
            Properties config = connectorManager.createConsumerConfig(
                    this.delayConfig.getGroup(), this.delayConfig.getUsername(),
                    this.delayConfig.getPassword(), this.delayConfig.getResetType()
            );
            config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.delayConfig.getMaxPollRecords());
            for (Map.Entry<String, Integer> entry : BASIC_DELAY_MAP.entrySet()) {
                KafkaDelayConsumer delayConsumer =
                        new KafkaDelayConsumer(
                                config, entry.getKey(), entry.getValue(), this::sendEvent,
                                this.delayConfig.getMaxPause(), this.delayConfig.getThreshold()
                        );
                this.executorService.submit(delayConsumer);
            }
        }
    }

    @Override
    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            if (null != this.executorService && !this.executorService.isShutdown()) {
                this.executorService.shutdown();
            }
        }
    }
}
