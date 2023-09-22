package cn.spear.event.web.security.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2023/3/3
 */
@Getter
@Setter
public class AuthingResp<T> {
    private Integer statusCode;
    private String message;
    private T data;
}
