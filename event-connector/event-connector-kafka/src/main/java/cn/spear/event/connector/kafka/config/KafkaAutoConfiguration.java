package cn.spear.event.connector.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luoyong
 * @date 2022/12/26
 */
@Configuration
public class KafkaAutoConfiguration {

    @Bean
    public AdminClient adminClient(KafkaConnectorProperties config) {
        return AdminClient.create(config.getAdminConfig());
    }
}
