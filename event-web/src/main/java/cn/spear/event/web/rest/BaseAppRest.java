package cn.spear.event.web.rest;

import cn.spear.event.core.Constants;
import cn.spear.event.core.annotation.EnableAuth;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.web.security.EventWebContext;
import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@EnableAuth(group = Constants.GROUP_APP)
public class BaseAppRest extends BaseRest {

    protected Long clientId() {
        return EventWebContext.getAttribute(Constants.CLAIM_CLIENT_ID, Long.class);
    }

    protected String clientSecret() {
        return EventWebContext.getAttribute(Constants.CLAIM_CLIENT_SECRET, String.class);
    }

    protected String privateId() {
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

    protected String requiredPrivateId() {
        String privateId = privateId();
        if (StrUtil.isBlank(privateId)) {
            throw new BusinessException("私有 ID 无效");
        }
        return privateId;
    }
}
