package cn.spear.event.connector.redis.delay;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.spear.event.connector.redis.config.RedisConnectorProperties;
import cn.spear.event.connector.redis.config.RedisDelayConfig;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.EventListener;
import cn.spear.event.core.connector.EventPersistent;
import cn.spear.event.core.connector.Producer;
import cn.spear.event.core.connector.callback.SendCallback;
import cn.spear.event.core.connector.context.AsyncConsumeContext;
import cn.spear.event.core.domain.event.SendResult;
import cn.spear.event.core.enums.EventAction;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luoyong
 * @date 2022/12/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDelayRunner implements EventListener, Runnable {
    private final StringRedisTemplate redisTemplate;
    private final EventPersistent eventPersistent;
    private final ConnectorManager connectorManager;
    private final RedisConnectorProperties config;

    @Override
    public boolean enableBatch() {
        return true;
    }

    @Override
    public void batchConsume(List<CloudEvent> eventList, AsyncConsumeContext context) {
        try {
            Set<ZSetOperations.TypedTuple<String>> valueSet = new HashSet<>();
            Map<String, CloudEvent> eventMap = new HashMap<>();
            for (CloudEvent cloudEvent : eventList) {
                // 存入 Redis
                long sendAt = Convert.toLong(cloudEvent.getExtension(Constants.ATTR_DELAY_SEND_TIME), 0L);
                if (sendAt > 0) {
                    valueSet.add(ZSetOperations.TypedTuple.of(cloudEvent.getId(), (double) sendAt));
                    // 存数据库
                    Object targetTopic = cloudEvent.getExtension(Constants.ATTR_DELAY_TARGET_TOPIC);
                    if (null != targetTopic) {
                        // 替换目标主题
                        cloudEvent = new CloudEventBuilder(cloudEvent)
                                .withSubject(targetTopic.toString())
                                .withoutExtension(Constants.ATTR_DELAY_TARGET_TOPIC)
                                .build();
                    }
                    eventMap.put(cloudEvent.getId(), cloudEvent);
                }
            }
            redisTemplate.opsForZSet().add(config.getDelay().getDelayQueueKey(), valueSet);
            eventPersistent.batchSave(eventMap);
            context.commit(EventAction.CommitMessage);
        } catch (Exception ex) {
            log.error("Redis 订阅消息异常", ex);
        }
    }

    @Override
    public void run() {
        final ZSetOperations<String, String> operations = redisTemplate.opsForZSet();
        RedisDelayConfig delayConfig = config.getDelay();
        final int fixedRate = delayConfig.getFixedRate();
        final int timeAccuracy = delayConfig.getTimeAccuracy();
        final int fetchCount = delayConfig.getMaxFetchCount();
        final String delayQueueKey = delayConfig.getDelayQueueKey();
        while (true) {
            process(operations, delayQueueKey, fixedRate, timeAccuracy, fetchCount);
        }
    }

    private void process(
            ZSetOperations<String, String> operations, String delayQueueKey,
            int fixedRate, int timeAccuracy, int fetchCount
    ) {
        try {
            long timestamp = System.currentTimeMillis() + timeAccuracy;
            Set<String> ids = operations
                    .rangeByScore(delayQueueKey, 0, timestamp, 0, fetchCount);
            if (log.isDebugEnabled()) {
                log.debug("开始 Redis 延迟消息执行定时任务:{},size:{}", timestamp, null == ids ? 0 : ids.size());
            }
            if (CollUtil.isEmpty(ids)) {
                ThreadUtil.safeSleep(fixedRate);
                return;
            }
            Long removeResult = operations.remove(delayQueueKey, ids.toArray(new Object[0]));
            if (Objects.isNull(removeResult) || removeResult <= 0) {
                return;
            }
            // todo 重试机制
            AtomicInteger count = new AtomicInteger(0);
            List<String> successIds = new ArrayList<>();
            Map<String, CloudEvent> eventMap = eventPersistent.batchGet(ids);
            Producer producer = connectorManager.createEventProducer();
            for (String id : ids) {
                CloudEvent cloudEvent = eventMap.get(id);
                if (Objects.isNull(cloudEvent)) {
                    continue;
                }
                producer.publish(cloudEvent, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("event[{}] send success", cloudEvent.getId());
                        successIds.add(id);
                    }

                    @Override
                    public void onException(ConnectorRuntimeException exception, SendResult sendResult) {
                        log.error("发布延时消息异常", exception);
                    }

                    @Override
                    public void onCompleted(SendResult sendResult) {
                        int i = count.incrementAndGet();
                        if (i == ids.size()) {
                            eventPersistent.batchRemove(successIds);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            log.error("Redis 延迟消息定时任务执行异常", ex);
        }
    }
}
