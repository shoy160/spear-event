package cn.spear.event.web.filter;

import cn.spear.event.business.service.ClientService;
import cn.spear.event.core.Constants;
import cn.spear.event.core.annotation.EnableAuth;
import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.lang.Tuple;
import cn.spear.event.core.utils.ReflectUtils;
import cn.spear.event.web.config.BaseProperties;
import cn.spear.event.web.security.AuthingClient;
import cn.spear.event.web.security.EventWebContext;
import cn.spear.event.web.security.model.AccessToken;
import cn.spear.event.web.security.model.AuthingUser;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author shay
 * @date 2020/9/1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final ClientService clientService;
    private final BaseProperties config;
    private final AuthingClient authingClient;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler
    ) {
        request.setAttribute(Constants.CLAIM_CLIENT_IP, EventWebContext.getClientIp(request));
        request.setAttribute(Constants.CLAIM_USER_AGENT, request.getHeader(Constants.HEADER_USER_AGENT));
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        //获取注解
        EnableAuth auth = ReflectUtils.getAnnotation(method.getMethod(), EnableAuth.class);
        if (auth == null) {
            return true;
        }
        boolean verifyResult = verifyAuth(auth, request);
        if (!auth.anonymous() && !verifyResult) {
            throw ResultCode.UN_AUTHORIZED.exception();
        }
        return true;
    }

    private boolean verifyAuth(@NonNull EnableAuth auth, @NonNull HttpServletRequest request) {
        String group = auth.group();
        request.setAttribute(Constants.CLAIM_PREFIX + Constants.CLAIM_GROUP, group);
        String authorization = request.getHeader(Constants.HEADER_AUTHORIZATION);
        switch (group) {
            case Constants.GROUP_APP:
                String privateId = getPrivateId();
                if (StrUtil.isNotBlank(privateId)) {
                    request.setAttribute(Constants.CLAIM_PREFIX + Constants.CLAIM_PRIVATE_ID, privateId);
                }
                Tuple<String, String> basicAuth = basicAuth(authorization);
                if (Objects.nonNull(basicAuth)) {
                    request.setAttribute(Constants.CLAIM_CLIENT_ID, basicAuth.getA());
                    request.setAttribute(Constants.CLAIM_CLIENT_SECRET, basicAuth.getB());
                }
                if (!config.isEnableClient()) {
                    return true;
                }
                if (Objects.isNull(basicAuth)) {
                    return false;
                }
                try {
                    clientService.verify(Convert.convert(Long.class, basicAuth.getA()), basicAuth.getB());
                } catch (BusinessException ex) {
                    if (!auth.anonymous()) {
                        throw ResultCode.UN_AUTHORIZED.exception(ex.getMessage());
                    }
                }
                return true;
            case Constants.GROUP_MANAGE:
                authingVerify(authorization, request);
                return true;
            default:
                break;
        }
        return true;
    }

    private Tuple<String, String> basicAuth(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        final String authSplit = Constants.STR_SPACE;
        final String clientSplit = Constants.STR_REGION;
        final int arrayLength = 2;
        String[] array = authorization.split(authSplit);
        if (array.length != arrayLength && !Constants.AUTHORIZATION_BASIC.equalsIgnoreCase(array[0])) {
            return null;
        }
        String clientInfo = Base64.decodeStr(array[1], StandardCharsets.UTF_8);
        String[] clientArray = clientInfo.split(clientSplit);
        if (clientArray.length != arrayLength) {
            return null;
        }
        return Tuple.of(clientArray[0], clientArray[1]);
    }

    private void authingVerify(String token, HttpServletRequest request) {
        if (Objects.equals(config.getManageToken(), token) || !config.isEnableAuth()) {
            return;
        }
        AccessToken accessToken = authingClient.getAccessToken(token);
        if (Objects.nonNull(accessToken)) {
            String userId = accessToken.getSub();
            request.setAttribute(Constants.CLAIM_USER_ID, userId);
            AuthingUser userInfo = authingClient.getUserInfo(userId, token);
            if (Objects.nonNull(userInfo)) {
                request.setAttribute(Constants.CLAIM_USERNAME, userInfo.getDisplayName());
            }
        }
        request.setAttribute(Constants.SESSION_TOKEN, token);
    }

    private String getPrivateId() {
        String privateId = Constants.STR_EMPTY;
        HttpServletRequest request = EventWebContext.getRequest();
        if (Objects.nonNull(request)) {
            privateId = request.getParameter(Constants.QUERY_PRIVATE_ID);
            if (StrUtil.isNotBlank(privateId)) {
                return privateId;
            }
            privateId = EventWebContext.getRequestHeader(Constants.HEADER_PRIVATE_ID);
            if (StrUtil.isNotBlank(privateId)) {
                return privateId;
            }
            privateId = EventWebContext.getRequestHeader(Constants.AUTHING_PRIVATE_ID);
        }
        return privateId;
    }
}
