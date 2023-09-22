package cn.spear.event.core.enums;


import cn.spear.event.core.exception.BusinessException;

/**
 * 状态码
 *
 * @author shay
 * @date 2020/7/15
 */
public enum ResultCode implements BaseNamedEnum {
    /**
     * 操作成功
     */
    SUCCESS(200, "success"),
    /**
     * 业务异常
     */
    FAILURE(400, "业务异常"),
    /**
     * 消息不能读取
     */
    MSG_NOT_READABLE(400, "消息不能读取"),

    /**
     * 缺少必要的请求参数
     */
    PARAM_MISS(400, "缺少必要的请求参数"),
    /**
     * 请求参数类型错误
     */
    PARAM_TYPE_ERROR(400, "请求参数类型错误"),
    /**
     * 请求参数绑定错误
     */
    PARAM_BIND_ERROR(400, "请求参数绑定错误"),
    /**
     * 参数校验失败
     */
    PARAM_VALID_ERROR(400, "参数校验失败"),

    /**
     * 租户不存在
     */
    NO_TENANT(400, "租户不存在"),

    /**
     * 请求未授权
     */
    UN_AUTHORIZED(401, "请求未授权"),
    /**
     * 请求被拒绝
     */
    REQ_REJECT(403, "请求被拒绝"),

    /**
     * 404 没找到请求
     */
    NOT_FOUND(404, "404 没找到请求"),

    /**
     * 不支持当前请求方法
     */
    METHOD_NOT_SUPPORTED(405, "不支持当前请求方法"),
    /**
     * 不支持当前媒体类型
     */
    MEDIA_TYPE_NOT_SUPPORTED(415, "不支持当前媒体类型"),

    /**
     * 服务器异常
     */
    INTERNAL_SERVER_ERROR(500, "服务器异常"),

    /**
     * 没有相关服务
     */
    NO_SERVICE(501, "没有相关服务");

    final int code;
    final String message;

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    ResultCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getName() {
        return this.message;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public BusinessException exception() {
        return new BusinessException(this);
    }

    public BusinessException exception(String message) {
        return new BusinessException(this, message);
    }
}
