package cn.spear.event.core.enums;

/**
 * 枚举序列化方式
 *
 * @author luoyong
 * @date 2021/12/17
 */
public enum EnumSerializerType implements BaseNamedEnum {
    /**
     * 字符串
     */
    String(0, "字符串"),
    /**
     * 编码
     */
    Code(1, "编码"),
    /**
     * 对象
     */
    Object(2, "对象");

    private final int code;
    private final String desc;

    EnumSerializerType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.desc;
    }
}
