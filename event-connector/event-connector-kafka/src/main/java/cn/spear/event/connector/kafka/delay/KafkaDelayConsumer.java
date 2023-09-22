package cn.spear.event.connector.kafka.delay;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.spear.event.core.Constants;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.lang.Action;
import io.cloudevents.CloudEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author luoyong
 * @date 2022/11/30
 */
@Slf4j
public class KafkaDelayConsumer implements Runnable {

    private volatile boolean isExited = false;
    private final Object lock = new Object();
    private final Properties config;
    private final String topic;
    private final long time;
    private final long maxPause;
    private final long threshold;
    private final Action<CloudEvent> eventSender;
    private final ScheduledExecutorService executorService;

    public KafkaDelayConsumer(Properties config, String topic, long time, @NonNull Action<CloudEvent> eventSender) {
        this(config, topic, time, eventSender, 5000L, 100L);
    }

    public KafkaDelayConsumer(
            Properties config, String topic, long time, @NonNull Action<CloudEvent> eventSender, long maxPause,
            long threshold
    ) {
        this.config = config;
        this.topic = topic;
        this.time = time;
        this.maxPause = maxPause;
        this.threshold = threshold;
        this.eventSender = eventSender;
        this.executorService = ThreadUtil.createScheduledExecutor(1);
    }

    public void exit() {
        this.isExited = true;
    }

    @Override
    public void run() {
        final KafkaConsumer<String, CloudEvent> consumer = new KafkaConsumer<>(this.config);
        consumer.subscribe(Collections.singletonList(this.topic));

        while (!isExited) {
            try {
                synchronized (lock) {
                    ConsumerRecords<String, CloudEvent> records = consumer.poll(Duration.ofSeconds(2));
                    if (records.count() > 0) {
                        log.info("{} consumer size:{}", this.topic, records.count());
                    }
                    long delayMs = 0;
                    ConsumerRecord<String, CloudEvent> delayRecord = null;
                    Map<TopicPartition, OffsetAndMetadata> commitMap = new HashMap<>();
                    for (ConsumerRecord<String, CloudEvent> record : records) {
                        long delay = calculateDelay(record);
                        if (delay <= this.threshold) {
                            this.eventSender.invoke(record.value());
                            commitMap.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                        } else {
                            if (delayMs == 0 || delayMs > delay) {
                                delayMs = delay;
                                delayRecord = record;
                            }
                        }
                    }
                    if (MapUtil.isNotEmpty(commitMap)) {
                        consumer.commitSync(commitMap);
                    }
                    if (null != delayRecord) {
                        if (log.isDebugEnabled()) {
                            log.debug("delay topic pause:{}", this.topic);
                        }
                        TopicPartition topicPartition = new TopicPartition(topic, delayRecord.partition());
                        consumer.pause(Collections.singleton(topicPartition));
                        consumer.seek(topicPartition, delayRecord.offset());
                        resume(consumer, delayMs);
                        lock.wait();
                    }
                }
            } catch (Exception ex) {
                log.error("延时队列监听异常", ex);
                if (!(ex instanceof BusinessException)) {
                    this.exit();
                }
            }
        }
    }

    private long calculateDelay(ConsumerRecord<String, CloudEvent> record) {
        long delay = record.timestamp() + this.time * 60 * 1000 - System.currentTimeMillis();
        Long sendTime = Convert.toLong(record.value().getExtension(Constants.ATTR_DELAY_SEND_TIME), 0L);
        if (sendTime > 0) {
            return Math.min(delay, sendTime - System.currentTimeMillis());
        }
        return delay;
    }

    private void resume(KafkaConsumer<String, CloudEvent> consumer, long delay) {
        final long consumerDelay = Math.min(this.maxPause, delay);
        if (log.isDebugEnabled()) {
            log.debug("topic {} will resume after {} ms", this.topic, consumerDelay);
        }
        this.executorService.schedule(() -> {
            synchronized (lock) {
                if (log.isDebugEnabled()) {
                    log.debug("delay topic resume:{}", this.topic);
                }
                consumer.resume(consumer.paused());
                lock.notify();
            }
        }, consumerDelay, TimeUnit.MILLISECONDS);
    }

    private void commitSync(KafkaConsumer<String, CloudEvent> consumer, ConsumerRecord<String, CloudEvent> record) {
        Map<TopicPartition, OffsetAndMetadata> commitMap = new HashMap<>(1);
        commitMap.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
        consumer.commitSync(commitMap);
    }
}
