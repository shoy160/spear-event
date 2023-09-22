package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 应用类型
 *
 * @author luoyong
 * @date 2023/2/16
 */
public enum AppTypeEnum implements BaseNamedEnum {
    /**
     * 系统应用
     */
    System(1, "系统"),
    /**
     * 自定义应用
     */
    Custom(2, "自定义");

    private final String name;
    @EnumValue
    private final Integer value;

    AppTypeEnum(int value, String name) {
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
