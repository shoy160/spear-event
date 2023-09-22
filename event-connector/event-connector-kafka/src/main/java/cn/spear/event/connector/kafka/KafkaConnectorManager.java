package cn.spear.event.connector.kafka;

import cn.spear.event.connector.kafka.config.KafkaConnectorProperties;
import cn.spear.event.connector.kafka.consumer.KafkaConsumer;
import cn.spear.event.connector.kafka.producer.KafkaProducer;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.Consumer;
import cn.spear.event.core.connector.Producer;
import cn.spear.event.core.connector.enums.ConsumerResetType;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.lang.Action;
import io.cloudevents.kafka.CloudEventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
@Component
public class KafkaConnectorManager implements ConnectorManager {
    private final KafkaConnectorProperties config;
    private final ConcurrentHashMap<String, Consumer> consumers;
    private final ConcurrentHashMap<String, Producer> producers;

    public KafkaConnectorManager(KafkaConnectorProperties config) {
        this.config = config;
        consumers = new ConcurrentHashMap<>();
        producers = new ConcurrentHashMap<>();
    }


    @Override
    public Consumer createConsumer(
            String group, String username, String password,
            ConsumerResetType resetType, Action<Properties> configAction
    ) {
        String consumerKey = String.format("%s_%s", username, group);
        Consumer consumer = this.consumers.get(consumerKey);
        if (null != consumer) {
            if (consumer.isStarted()) {
                consumer.shutdown();
            }
            return consumer;
        }
        Properties config = this.createConsumerConfig(group, username, password, resetType);
        if (Objects.nonNull(configAction)) {
            configAction.invoke(config);
        }
        KafkaConsumer kafkaConsumer = new KafkaConsumer();
        kafkaConsumer.init(config);
        this.consumers.put(consumerKey, kafkaConsumer);
        return kafkaConsumer;
    }

    @Override
    public Producer createProducer(String username, String password) {
        return this.producers.computeIfAbsent(username, k -> {
            Properties keyValue = new Properties();
            this.config.createSaslJaasConfig(keyValue, username, password);
            this.config.createProducer(keyValue);
            KafkaProducer kafkaProducer = new KafkaProducer();
            kafkaProducer.init(keyValue);
            return kafkaProducer;
        });
    }

    @Override
    public Properties createConsumerConfig(String group, LanguageType language, String username, String password) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        if (language == LanguageType.Java) {
            this.config.createConsumer(properties);
            if (this.config.isEnableAcl()) {
                this.config.createSaslJaasConfig(properties, username, password);
            }
        } else {
            properties.put("metadata.broker.list", this.config.getNamesrvAddr());
            this.config.createSaslConfig(properties, username, password);
        }
        return properties;
    }

    public Producer createEventProducer() {
        return createProducer(config.getUsername(), config.getPassword());
    }

    @Override
    public Properties createConsumerConfig(String group, String username, String password, ConsumerResetType resetType) {
        Properties keyValue = new Properties();
        keyValue.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        this.config.createSaslJaasConfig(keyValue, username, password);
        this.config.createConsumer(keyValue);
        if (null != resetType) {
            //earliest,latest,none
            keyValue.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetType.name().toLowerCase());
        }
        keyValue.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
        keyValue.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        keyValue.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return keyValue;
    }
}
