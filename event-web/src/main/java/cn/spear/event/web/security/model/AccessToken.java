package cn.spear.event.web.security.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author luoyong
 * @date 2022/12/20
 */
@Getter
@Setter
@ToString
public class AccessToken {

    /**
     * 令牌标识符声明
     */
    private String jti;

    /**
     * subject 的缩写，唯一标识，一般为用户 ID
     */
    private String sub;

    /**
     * “Issued At”表示针对此令牌进行身份验证的时间。
     */
    private String iat;

    /**
     * “exp”（过期时间）声明指定只能在哪个时间（含）之前接受 JWT 的处理。
     */
    private String exp;

    /**
     * 应用侧向 Authing 请求的权限
     */
    private String scope;

    /**
     * 标识构造并返回令牌的安全令牌服务 (STS)
     */
    private String iss;

    /**
     * 标识令牌的目标接收方
     */
    private String aud;
}
