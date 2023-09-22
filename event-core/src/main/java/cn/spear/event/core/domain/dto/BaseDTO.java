package cn.spear.event.core.domain.dto;

import cn.spear.event.core.utils.JsonUtils;

import java.io.Serializable;

/**
 * @author shay
 * @date 2020/10/13
 */
public abstract class BaseDTO implements Serializable {
    private static final long serialVersionUID = 5248467646828511238L;

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
