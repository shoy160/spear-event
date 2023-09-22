package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author luoyong
 * @date 2023/1/5
 */
public enum EventTypeEnum implements BaseNamedEnum {
    /**
     * 公共事件
     */
    Public(1, "公共事件"),
    /**
     * 私有事件
     */
    Private(2, "私有事件");

    private final String name;
    @EnumValue
    private final Integer value;

    EventTypeEnum(int value, String name) {
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
