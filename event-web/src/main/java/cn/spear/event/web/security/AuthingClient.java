package cn.spear.event.web.security;

import cn.spear.event.core.Constants;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.http.HttpHelper;
import cn.spear.event.core.http.HttpRequest;
import cn.spear.event.core.http.HttpResponse;
import cn.spear.event.core.http.enums.HttpMethod;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.web.config.BaseProperties;
import cn.spear.event.web.security.model.AccessToken;
import cn.spear.event.web.security.model.AuthingResp;
import cn.spear.event.web.security.model.AuthingUser;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author luoyong
 * @date 2023/3/3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthingClient {
    private final BaseProperties config;
    private final Cache<String, AuthingUser> cache;
    private JWKSet jwkSet;

    public AuthingUser getUserInfo(String userId, String accessToken) {
        try {
            String cacheKey = String.format("user:%s", userId);
            AuthingUser user = cache.get(cacheKey);
            if (Objects.nonNull(user)) {
                return user;
            }
            BaseProperties.AuthConfig authConfig = config.getAuthConfig();
            String url = URLUtil.completeUrl(authConfig.getDomain(), "/api/v3/get-profile");
            HttpRequest request = new HttpRequest(url, HttpMethod.GET);
            request.setHeader("Authorization", "Bearer ".concat(accessToken));
            request.setHeader("x-spear-app-id", authConfig.getAppId());
            HttpResponse resp = HttpHelper.request(request);
            if (resp.isSuccessCode()) {
                byte[] body = resp.getBody();
                AuthingResp<AuthingUser> result =
                        JsonUtils.json(body, f -> f.constructParametricType(AuthingResp.class, AuthingUser.class));
                if (Objects.nonNull(result)) {
                    if (Objects.equals(Constants.CODE_SUCCESS, result.getStatusCode())) {
                        user = result.getData();
                        cache.put(cacheKey, user, authConfig.getUserCacheTimeout(), TimeUnit.MINUTES);
                        return user;
                    }
                    log.warn("获取 Authing 用户资料失败: {}", result.getMessage());
                } else {
                    log.warn("获取 Authing 用户资料异常：{}", new String(body));
                }
            }
        } catch (Exception ex) {
            log.warn("获取 Authing 用户信息异常");
        }
        return null;

    }

    public AccessToken getAccessToken(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String payload;
            JWKSet jwkSet = fetchJwkSet();
            if (Objects.isNull(jwkSet)) {
                throw new BusinessException("获取登录凭证信息异常");
            }
            RSAKey rsaKey = jwkSet.getKeys().get(0).toRSAKey();
            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!jwsObject.verify(verifier)) {
                throw new BusinessException("校验不通过");
            }
            payload = jwsObject.getPayload().toString();
            AccessToken accessToken = JsonUtils.json(payload, AccessToken.class);
            if (null == accessToken) {
                throw ResultCode.UN_AUTHORIZED.exception();
            }
            // 过期时间
            String exp = accessToken.getExp();
            if (StrUtil.isNotBlank(exp)) {
                if (System.currentTimeMillis() >= Convert.toLong(exp, 0L) * 1000L) {
                    throw ResultCode.UN_AUTHORIZED.exception("登录凭证已失效");
                }
            }
            return accessToken;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Token 解析异常", ex);
            throw new BusinessException("登录凭证解析异常");
        }
    }

    private JWKSet fetchJwkSet() {
        if (null != this.jwkSet) {
            return this.jwkSet;
        } else {
            try {
                BaseProperties.AuthConfig authConfig = this.config.getAuthConfig();
                return this.jwkSet = JWKSet.load(new URL(authConfig.getDomain() + "/oidc/.well-known/jwks.json"));
            } catch (Exception ex) {
                log.warn("获取 oidc jwks 异常", ex);
                return null;
            }
        }
    }
}
