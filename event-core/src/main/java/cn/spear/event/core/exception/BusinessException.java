package cn.spear.event.core.exception;

import cn.spear.event.core.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author shay
 * @date 2020/7/15
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    public BusinessException(String message) {
        this(ResultCode.FAILURE.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
