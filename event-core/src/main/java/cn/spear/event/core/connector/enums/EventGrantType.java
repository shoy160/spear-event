package cn.spear.event.core.connector.enums;

import cn.spear.event.core.enums.BaseNamedEnum;

/**
 * @author luoyong
 * @date 2022/11/10
 */
public enum EventGrantType implements BaseNamedEnum {
    /**
     * 发布
     */
    Producer(1, "发布"),
    /**
     * 订阅
     */
    Consumer(2, "订阅"),

    ProducerAndConsumer(3, "发布订阅");

    private final String name;
    private final Integer value;

    EventGrantType(int value, String name) {
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
