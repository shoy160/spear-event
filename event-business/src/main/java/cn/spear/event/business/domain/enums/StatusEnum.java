package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author luoyong
 * @date 2022/11/10
 */
public enum StatusEnum implements BaseNamedEnum {
    /**
     * 启用
     */
    Enabled(1, "启用"),
    /**
     * 停用
     */
    Disabled(4, "停用");

    private final String name;
    @EnumValue
    private final Integer value;

    StatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
