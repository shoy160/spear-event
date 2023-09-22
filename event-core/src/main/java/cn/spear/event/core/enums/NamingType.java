package cn.spear.event.core.enums;

/**
 * 命名方式
 *
 * @author shay
 * @date 2020/11/11
 */
public enum NamingType implements BaseEnum {
    /**
     * Normal
     */
    Normal(0),
    /**
     * 驼峰命名
     */
    CamelCase(1),
    /**
     * 蛇形命名
     */
    SnakeCase(2),
    /**
     * 大写
     */
    UpperCase(3);

    private final int value;

    NamingType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
