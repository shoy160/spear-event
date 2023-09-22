package cn.spear.event.core.domain.dto;

import cn.spear.event.core.enums.ResultCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 标准结果实体
 *
 * @author shay
 * @date 2020/7/15
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel("ResultDTO")
public class ResultDTO<T> implements Serializable {
    private static final long serialVersionUID = -831678996819330527L;

    /**
     * 状态码
     */
    @ApiModelProperty(value = "状态码，200：成功；400: 业务异常；500: 系统异常", example = "200")
    private Integer code;

    /**
     * 成功标示
     */
    @ApiModelProperty(value = "成功标识", example = "true")
    private Boolean success;

    @ApiModelProperty("错误消息")
    private String message;
    /**
     * 数据
     */
    @ApiModelProperty("响应数据")
    private T data;
    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳", example = "1672122280612")
    private Long timestamp;

    private ResultDTO(T data) {
        this.success = true;
        this.code = 200;
        this.data = data;
        this.message = "success";
        this.timestamp = System.currentTimeMillis();
    }

    private ResultDTO(String message, int code) {
        this.success = code == 200;
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    private ResultDTO(String message, int code, T data) {
        this(message, code);
        this.data = data;
    }

    private static <T> ResultDTO<T> result(ResultCode resultCode, String message) {
        return new ResultDTO<>(message, resultCode.getCode());
    }

    private static <T> ResultDTO<T> result(ResultCode resultCode) {
        return new ResultDTO<>(resultCode.getMessage(), resultCode.getCode());
    }

    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(data);
    }

    public static <T> ResultDTO<T> success() {
        return new ResultDTO<>();
    }

    public static <T> ResultDTO<T> successT() {
        return result(ResultCode.SUCCESS);
    }

    public static <T> ResultDTO<T> failT(int code, String message) {
        return new ResultDTO<>(message, code);
    }

    public static <T> ResultDTO<T> failT(ResultCode resultCode) {
        return new ResultDTO<>(resultCode.getMessage(), resultCode.getCode());
    }

    public static <T> ResultDTO<T> failT(ResultCode resultCode, String message) {
        return new ResultDTO<>(message, resultCode.getCode());
    }

    public static <T> ResultDTO<T> fail(int code, String message) {
        return new ResultDTO<>(message, code, null);
    }

    public static <T> ResultDTO<T> fail(ResultCode resultCode) {
        return new ResultDTO<>(resultCode.getMessage(), resultCode.getCode(), null);
    }

    public static <T> ResultDTO<T> fail(ResultCode resultCode, String message) {
        return new ResultDTO<>(message, resultCode.getCode(), null);
    }
}
