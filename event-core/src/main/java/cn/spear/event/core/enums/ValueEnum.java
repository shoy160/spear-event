package cn.spear.event.core.enums;

import java.io.Serializable;

/**
 * 枚举类型,只有value
 *
 * @author shay
 * @date 2020/8/8
 */
public interface ValueEnum<T> extends Serializable {
    /**
     * 获取枚举值
     *
     * @return 枚举值
     */
    T getValue();
}
