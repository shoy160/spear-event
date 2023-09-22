package cn.spear.event.connector.redis.delay;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.connector.redis.config.RedisConnectorProperties;
import cn.spear.event.connector.redis.config.RedisDelayConfig;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.*;
import cn.spear.event.core.connector.enums.EventGrantType;
import cn.spear.event.core.connector.enums.EventPatternType;
import cn.spear.event.core.connector.model.EventAclBinding;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luoyong
 * @date 2022/12/8
 */
@Slf4j
@Component
public class RedisDelayManager implements DelayManager {
    private final AtomicBoolean started = new AtomicBoolean(false);

    private final ConnectorManager connectorManager;
    private final RedisDelayConfig config;
    private final ConnectorAcl connectorAcl;
    private final RedisDelayRunner delayRunner;

    private Consumer consumer;


    public RedisDelayManager(
            ConnectorManager connectorManager, RedisConnectorProperties connectorConfig,
            ConnectorAcl connectorAcl, RedisDelayRunner delayRunner) {
        this.connectorManager = connectorManager;
        this.config = connectorConfig.getDelay();
        this.connectorAcl = connectorAcl;
        this.delayRunner = delayRunner;
        if (config.isEnable()) {
            this.initDelayConfig();
            this.start();
        } else {
            log.info("Redis 延迟队列未启用");
        }
    }


    private void initDelayConfig() {
        log.info("init redis delay topic:{}", config.getDelayTopic());
        String delayGroup = this.config.getConsumerGroup();
        String username = this.config.getUsername();
        boolean createResult = this.connectorAcl.createUser(username, this.config.getPassword());
        if (createResult) {
            EventAclBinding aclBinding = new EventAclBinding();
            aclBinding.setUser(username);
            aclBinding.setGrantType(EventGrantType.ProducerAndConsumer);
            aclBinding.setPatternType(EventPatternType.Literal);
            aclBinding.setTopic(config.getDelayTopic());
            aclBinding.setGroup(delayGroup);
            this.connectorAcl.addAcl(aclBinding);
        }
        this.connectorAcl.createTopic(
                config.getDelayTopic(), config.getPartitions(), config.getReplications(),
                config.getRetention()
        );
    }

    @Override
    public void sendDelay(@NonNull CloudEvent cloudEvent, Date delayAt) {
        String topic = cloudEvent.getSubject();
        if (StrUtil.isBlank(topic)) {
            throw new ConnectorRuntimeException("Topic 不能为空");
        }
        CloudEvent event = new CloudEventBuilder(cloudEvent)
                .withSubject(config.getDelayTopic())
                .withoutExtension(Constants.ATTR_DELAY_MINUTES)
                .withExtension(Constants.ATTR_DELAY_TARGET_TOPIC, topic)
                .withExtension(Constants.ATTR_DELAY_SEND_TIME, delayAt.getTime())
                .build();
        Producer producer = connectorManager.createProducer(config.getUsername(), config.getPassword());
        producer.sendOneway(event);
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
            try {
                ThreadUtil.newFixedExecutor(config.getMaxThreads(), "redis-delay", true)
                        .submit(this.delayRunner);
                consumer =
                        connectorManager.createConsumer(
                                config.getConsumerGroup(), config.getUsername(), config.getPassword(),
                                c -> c.put("max.poll.records", config.getMaxPollRecords())
                        );
                consumer.registerEventListener(this.delayRunner);
                consumer.subscribe(config.getDelayTopic());
                consumer.start();
            } catch (Exception ex) {
                log.error("Redis 延迟队列监听启动失败", ex);
            }
        }
    }

    @Override
    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            if (Objects.nonNull(consumer)) {
                consumer.shutdown();
            }
        }
    }
}
