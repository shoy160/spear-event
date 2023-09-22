package cn.spear.event.core.session;

import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.core.utils.TypeUtils;
import cn.hutool.core.convert.Convert;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;

/**
 * @author shay
 * @date 2021/3/1
 */
public interface Session {

    /**
     * 获取用户ID
     *
     * @return UserId
     */
    Object getUserId();

    /**
     * 获取租户ID
     *
     * @return tenantId
     */
    Object getTenantId();

    /**
     * 获取用户名
     *
     * @return UserName
     */
    String getUserName();

    /**
     * 获取用户权限
     *
     * @return Role
     */
    String getRole();

    /**
     * 获取租户站点
     *
     * @return Side
     */
    default TenancySide getSide() {
        return Objects.nonNull(getTenantId()) ? TenancySide.Tenant : TenancySide.Host;
    }

    /**
     * 获取所有声明
     *
     * @return map
     */
    Map<String, Object> getClaims();

    /**
     * 获取声明值
     *
     * @param key key
     * @return value
     */
    Object getClaim(String key);


    /**
     * 设置申明值
     *
     * @param key   key
     * @param value value
     */
    void setClaim(String key, Object value);

    /**
     * 获取声明
     *
     * @param key key
     * @return value
     */
    default String getClaimAsString(String key) {
        Object value = getClaim(key);
        if (null == value) {
            return null;
        }
        if (TypeUtils.isSimple(value)) {
            return value.toString();
        }
        return JsonUtils.toJson(value);
    }

    /**
     * 获取声明值
     *
     * @param key   key
     * @param clazz class
     * @param <T>   T
     * @return 声明值
     */
    default <T> T getClaim(String key, Class<T> clazz) {
        Object value = getClaim(key);
        if (Objects.isNull(value)) {
            return null;
        }
        return Convert.convert(clazz, value);
    }


    /**
     * 获取用户ID
     *
     * @param clazz        clazz
     * @param defaultValue def
     * @param <T>          T
     * @return instance
     */
    default <T> T userId(Class<T> clazz, T defaultValue) {
        Object userId = getUserId();
        if (userId == null || CommonUtils.isEmpty(userId)) {
            return defaultValue;
        }
        return Convert.convert(clazz, userId, defaultValue);
    }

    /**
     * 获取用户ID
     *
     * @return String
     */
    default String userIdAsString() {
        return userId(String.class, null);
    }

    /**
     * 获取用户ID
     *
     * @return Integer
     */
    default Integer userIdAsInteger() {
        return userId(Integer.class, null);
    }

    /**
     * 获取用户ID
     *
     * @return Long
     */
    default Long userIdAsLong() {
        return userId(Long.class, null);
    }

    /**
     * 获取用户ID
     *
     * @param clazz clazz
     * @param <T>   T
     * @return instance
     */
    default <T> T requiredUserId(Class<T> clazz) {
        T userId = userId(clazz, null);
        if (userId == null || CommonUtils.isEmpty(userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED);
        }
        return userId;
    }

    /**
     * 获取租户ID
     *
     * @param clazz        clazz
     * @param defaultValue def
     * @param <T>          T
     * @return instance
     */
    default <T> T tenantId(Class<T> clazz, T defaultValue) {
        Object userId = getTenantId();
        if (userId == null || CommonUtils.isEmpty(userId)) {
            return defaultValue;
        }
        return Convert.convert(clazz, userId, defaultValue);
    }

    /**
     * 获取租户ID
     *
     * @return String
     */
    default String tenantIdAsString() {
        return tenantId(String.class, null);
    }

    /**
     * 获取租户ID
     *
     * @return String
     */
    default Integer tenantIdAsInteger() {
        return tenantId(Integer.class, null);
    }

    /**
     * 获取租户ID
     *
     * @return String
     */
    default Long tenantIdAsLong() {
        return tenantId(Long.class, null);
    }

    /**
     * 获取租户ID
     *
     * @param clazz clazz
     * @param <T>   T
     * @return instance
     */
    default <T> T requiredTenantId(Class<T> clazz) {
        T tenantId = tenantId(clazz, null);
        if (tenantId == null || CommonUtils.isEmpty(tenantId)) {
            throw new BusinessException(ResultCode.NO_TENANT);
        }
        return tenantId;
    }

    /**
     * 使用Session
     *
     * @param session session
     * @return closeable
     */
    Closeable use(SessionDTO session);
}
