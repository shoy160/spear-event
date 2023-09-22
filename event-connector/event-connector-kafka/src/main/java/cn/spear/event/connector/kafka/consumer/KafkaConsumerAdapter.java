package cn.spear.event.connector.kafka.consumer;

import cn.hutool.core.thread.ThreadUtil;
import cn.spear.event.core.connector.EventListener;
import cn.spear.event.core.connector.context.AbstractContext;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
public class KafkaConsumerAdapter {
    private final KafkaConsumer<String, CloudEvent> kafkaConsumer;
    private final Properties properties;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final KafkaConsumerRunner kafkaConsumerRunner;
    private final ExecutorService executorService;
    private final Set<String> topicsSet;

    public KafkaConsumerAdapter(final Properties properties) {

        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        this.properties = properties;
        this.kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumerRunner = new KafkaConsumerRunner(this.kafkaConsumer);
        executorService = ThreadUtil.newFixedExecutor(10, "k-consumer", true);
        topicsSet = new HashSet<>();
    }

    public Properties attributes() {
        return properties;
    }

    public void start() {
        if (this.started.compareAndSet(false, true)) {
            executorService.submit(kafkaConsumerRunner);
        }
    }


    public synchronized void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.kafkaConsumerRunner.shutdown();
//            this.kafkaConsumer.close();
        }
    }


    public boolean isStarted() {
        return this.started.get();
    }


    public boolean isClosed() {
        return !this.isStarted();
    }

    public KafkaConsumer<String, CloudEvent> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public synchronized void subscribe(String topic) {
        try {
            topicsSet.add(topic);
            List<String> topics = new ArrayList<>(topicsSet);
            this.kafkaConsumer.subscribe(topics);
        } catch (Exception e) {
            log.error("Error while subscribing the Kafka consumer to topic: ", e);
            throw new ConnectorRuntimeException(
                    String.format("Kafka consumer can't attach to %s.", topic));
        }
    }

    public synchronized void unsubscribe(String topic) {
        try {
            // Kafka will unsubscribe *all* topic if calling unsubscribe, so we
            this.kafkaConsumer.unsubscribe();
            topicsSet.remove(topic);
            List<String> topics = new ArrayList<>(topicsSet);
            this.kafkaConsumer.subscribe(topics);
        } catch (Exception e) {
            log.error("Error while unsubscribing the Kafka consumer: ", e);
            throw new ConnectorRuntimeException(String.format("kafka push consumer fails to unsubscribe topic: %s", topic));
        }
    }

    public void updateOffset(List<CloudEvent> cloudEvents, AbstractContext context) {
        cloudEvents.stream()
                .map(this.kafkaConsumerRunner::getOffset)
                .max(Long::compare)
                .ifPresent(aLong -> cloudEvents.forEach(cloudEvent ->
                        this.updateOffset(cloudEvent.getSubject(), aLong)));
    }

    public void updateOffset(String topicName, long offset) {
        this.kafkaConsumer.seek(new TopicPartition(topicName, 1), offset);
    }

    public void registerEventListener(EventListener listener) {
        this.kafkaConsumerRunner.setListener(listener);
    }
}
