package cn.spear.event.example;

import cn.authing.event.core.utils.JsonUtils;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.LogLevelConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * event 消费者 基于 kafka-clients
 *
 * @author luoyong
 * @date 2022/11/21
 */
public class eventReceiver {

    @Test
    public void listenerTest() {
        // 事件中台提供参数
        String eventServers = "chengdu.y2b.site:18083";
        String eventTopic = "user.created";
        String eventGroup = "authing-server";
        String clientId = "4168053424263168";
        String clientSecret = "c9db25d9b9f548968d06d7a7f56cd3ea";

        Properties config = new Properties();
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 是否自动提交
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // 订阅方式 earliest,latest,none
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // 单次最大消费数
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);

        // 服务地址
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, eventServers);
        // 消费组设置
        config.put(ConsumerConfig.GROUP_ID_CONFIG, eventGroup);
        // SASL 认证设置
        config.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        // SCRAM-SHA-256 OR SCRAM-SHA-512
        config.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");

        String saslConfig =
                String.format(
                        "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                        clientId,
                        clientSecret);
        config.put(SaslConfigs.SASL_JAAS_CONFIG, saslConfig);

        KafkaConsumer<String, CloudEvent> consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Collections.singletonList(eventTopic));

        while (true) {
            try {
                ConsumerRecords<String, CloudEvent> records = consumer.poll(Duration.ofSeconds(5));
//                if (records.isEmpty()) {
//                    break;
//                }
                // 事件处理
                records.forEach(rec -> {
                    CloudEvent cloudEvent = rec.value();
                    final long offset = rec.offset();
                    String topicName = cloudEvent.getSubject();
                    Object data = null;
                    if (null != cloudEvent.getData()) {
                        data = JsonUtils.json(cloudEvent.getData().toBytes(), Object.class);
                    }
                    System.out.printf("receive %s:%s -> %s,%s%n", topicName, offset, cloudEvent.getId(), data);
                });
                // 确认消费成功之后同步提交
//                consumer.commitSync();
            } catch (WakeupException ex) {
                break;
            }
        }
    }
}
