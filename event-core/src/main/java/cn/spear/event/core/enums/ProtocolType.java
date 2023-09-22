package cn.spear.event.core.enums;

/**
 * 服务协议
 *
 * @author luoyong
 * @date 2021/6/4
 */
public enum ProtocolType implements BaseNamedEnum {
    /**
     * HTTP
     */
    HTTP(1, "HTTP"),
    TCP(2, "TCP"),
    ;
    private final int value;
    private final String name;

    ProtocolType(int value, String name) {
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
