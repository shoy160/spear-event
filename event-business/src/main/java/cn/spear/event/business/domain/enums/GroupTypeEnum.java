package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 应用类型
 *
 * @author luoyong
 * @date 2023/2/16
 */
public enum GroupTypeEnum implements BaseNamedEnum {
    /**
     * 分组
     */
    Group(1, "分组"),
    /**
     * 标签
     */
    Tag(2, "标签");

    private final String name;
    @EnumValue
    private final Integer value;

    GroupTypeEnum(int value, String name) {
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
