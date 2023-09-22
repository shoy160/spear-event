package cn.spear.event.connector.kafka.config;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ScramMechanism;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Kafka 配置
 *
 * @author luoyong
 * @date 2022/11/9
 */
@Getter
@Setter
@Slf4j
@Component
@ConfigurationProperties(prefix = "spear.connector.kafka")
public class KafkaConnectorProperties {
    private String namesrvAddr = "";
    /**
     * 是否开启 ACL
     */
    public boolean enableAcl = true;
    private String username = "";
    private String password = "";
    /**
     * 默认副本数
     */
    private int replications = 1;
    /**
     * 最大分片数
     */
    private int maxPartitions = 16;
    private Integer consumeThreadMin = 2;
    private Integer consumeThreadMax = 2;
    private Integer consumeQueueSize = 10000;
    private Integer pullBatchSize = 32;
    private Integer ackWindow = 1000;
    private Integer pubWindow = 100;
    private long consumeTimeout = 0L;
    private Integer pollNameServerInterval = 10 * 1000;
    private Integer heartbeatBrokerInterval = 30 * 1000;
    private Integer rebalanceInterval = 20 * 1000;
    private String clusterName = "";
    private String accessKey = "";
    private String secretKey = "";
    private ScramMechanism mechanism = ScramMechanism.SCRAM_SHA_256;
    /**
     * 延迟队列配置
     */
    @NestedConfigurationProperty
    private KafkaDelayConfig delay = new KafkaDelayConfig();

    public String getScramMechanism() {
        return this.mechanism.name().replace("_", "-");
    }

    public void createProducer(Properties properties) {
        properties.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.namesrvAddr);
    }

    public void createConsumer(Properties properties) {
        properties.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.namesrvAddr);
    }

    public Properties getAdminConfig() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.namesrvAddr);
        createSaslJaasConfig(config, this.username, this.password);
        return config;
    }

    public void createSaslJaasConfig(Properties config, String username, String password) {
        if (!this.enableAcl) {
            username = this.username;
            password = this.password;
        }
        if (StrUtil.isBlank(username)) {
            return;
        }
        config.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        config.put(SaslConfigs.SASL_MECHANISM, getScramMechanism());
        String saslConfig = String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";", username, password);
        config.put(SaslConfigs.SASL_JAAS_CONFIG, saslConfig);
    }

    public void createSaslConfig(Properties config, String username, String password) {
        if (!this.enableAcl) {
            username = this.username;
            password = this.password;
        }
        if (StrUtil.isBlank(username)) {
            return;
        }
        config.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        config.put("sasl.mechanism", getScramMechanism());
        config.put("sasl.username", username);
        config.put("sasl.password", password);
    }
}
