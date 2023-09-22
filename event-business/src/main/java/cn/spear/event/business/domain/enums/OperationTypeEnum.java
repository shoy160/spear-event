package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author luoyong
 * @date 2022/11/10
 */
public enum OperationTypeEnum implements BaseNamedEnum {
    /**
     * 创建
     */
    CREATE(1, "创建"),
    /**
     * 写
     */
    WRITE(2, "写"),
    /**
     * 读
     */
    READ(3, "读");

    private final String name;
    @EnumValue
    private final Integer value;

    OperationTypeEnum(int value, String name) {
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
