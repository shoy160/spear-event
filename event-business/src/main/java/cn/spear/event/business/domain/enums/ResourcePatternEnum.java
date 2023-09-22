package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 资源匹配类型
 *
 * @author luoyong
 * @date 2022/12/1
 */
public enum ResourcePatternEnum implements BaseNamedEnum {
    /**
     * 精确模式
     */
    Literal(1, "精确模式"),
    /**
     * 前缀模式
     */
    Prefixed(2, "前缀模式");

    private final String name;
    @EnumValue
    private final Integer value;

    ResourcePatternEnum(int value, String name) {
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
