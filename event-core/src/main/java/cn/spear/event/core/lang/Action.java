package cn.spear.event.core.lang;

/**
 * todo
 *
 * @author shay
 * @date 2020/8/15
 */
public interface Action<T> {
    /**
     * 执行方法
     *
     * @param source 参数
     */
    void invoke(T source);
}
