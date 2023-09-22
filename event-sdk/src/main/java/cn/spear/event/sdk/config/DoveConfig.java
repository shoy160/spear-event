package cn.spear.event.sdk.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2023/2/9
 */
@Getter
@Setter
public class eventConfig {
    /**
     * event Host
     */
    private String host;
    /**
     * 客户端 ID
     */
    private String clientId;
    /**
     * 客户端密钥
     */
    private String clientSecret;
}
