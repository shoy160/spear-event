package cn.spear.event.web.config;

import cn.spear.event.core.enums.EnumSerializerType;
import cn.spear.event.core.enums.TimestampType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author shay
 * @date 2020/8/20
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spear")
public class BaseProperties {
    /**
     * 时间戳格式(默认:Second，可选值：None,Second,MilliSecond)
     */
    private TimestampType timestamp = TimestampType.Second;

    /**
     * 时间格式化，时间戳格式为None时生效，默认值：yyyy-MM-dd HH:mm:ss
     */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 枚举值序列化
     */
    private EnumSerializerType enumSerializer = EnumSerializerType.Code;

    /**
     * 长整型序列化时转为字符类型
     */
    private boolean longToString = true;

    /**
     * 是否开启空值处理
     */
    private boolean enableNullValue = false;

    private String swaggerBasePath = "/";
    private String swaggerToken = "";
    private boolean enableCors = true;
    private String corsPath = "/**";
    private String corsMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    private String corsOrigin = "*";
    private String corsHeaders = "Token,Jwt-Token,Language,Content-Type,Authorization,Referer";

    private boolean enableAuth = true;
    /**
     * 是否开启客户端验证，默认开启
     */
    private boolean enableClient = true;
    private boolean enableHistory = true;

    private String manageToken = "Bearer Spear-Event";

    private AuthConfig authConfig = new AuthConfig();

    @Getter
    @Setter
    public static class AuthConfig {
        /**
         * 用户池 ID
         */
        private String userPoolId;
        /**
         * 应用 ID
         */
        private String appId;
        /**
         * 应用密钥
         */
        private String appSecret;
        /**
         * spear Domain
         */
        private String domain;
        /**
         * 跳转地址
         */
        private String redirectUri;

        private boolean useImplicitMode;

        /**
         * 用户信息缓存时间(分钟)
         * 默认：20
         */
        private int userCacheTimeout = 20;

    }
}
