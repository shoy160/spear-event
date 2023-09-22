package cn.spear.event.business.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author leizeyu
 * @date 2023/3/8
 */
@Getter
@Setter
@ToString
public class AuthInfoDTO {

    private Boolean isSDK;

    private AuthConfig config;

    @Getter
    @Setter
    private static class AuthConfig {
        private String userPoolId;
        private String appId;
        private String domain;
        private String redirectUri;
        private Boolean useImplicitMode;
    }
}
