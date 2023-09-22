package cn.spear.event.core.enums;

/**
 * 枚举类型,有value,name
 *
 * @author shay
 * @date 2020/8/8
 */
public interface ValueNameEnum<T> extends ValueEnum<T> {

    /**
     * 获取枚举名称
     *
     * @return 枚举名称
     */
    String getName();
}
