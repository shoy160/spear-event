package cn.spear.event.business.config;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author luoyong
 * @date 2023/2/22
 */
@Getter
@Setter
@Slf4j
@Component
@ConfigurationProperties(prefix = "spear.event")
public class EventProperties {
    /**
     * Schema 模板
     */
    private String schemaTemplate;
    /**
     * 数据库类型
     */
    private DbType dbType = DbType.POSTGRE_SQL;

    @NestedConfigurationProperty
    private CustomAppConfig customApp = new CustomAppConfig();

    @Getter
    @Setter
    public static class CustomAppConfig {
        /**
         * 自定义应用编码前缀
         */
        private String prefix = "custom_";
        /**
         * 自定义应用编码长度
         */
        private int length = 5;

        /**
         * 默认应用编码生成超时时间（毫秒）
         */
        private long timeout = 1500;
        private String logo = "https://files.authing.co/workflow/custom.svg";
    }
}
