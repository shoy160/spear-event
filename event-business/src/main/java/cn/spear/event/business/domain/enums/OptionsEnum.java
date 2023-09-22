package cn.spear.event.business.domain.enums;

import cn.spear.event.core.enums.BaseNamedEnum;

/**
 * @author luoyong
 * @date 2023/4/23
 */
public enum OptionsEnum implements BaseNamedEnum {
    /**
     * 无配置
     */
    None("无配置", 0),
    /**
     * 可编排
     */
    Arrangeable("可编排", 1);

    private final String name;
    private final int value;

    OptionsEnum(String name, int value) {
        this.name = name;
        this.value = value;
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
