package cn.spear.event.core.lang;

/**
 * Func with Exception
 *
 * @author shay
 * @date 2020/8/15
 */
@FunctionalInterface
public interface FuncWithException<R, T, E extends Throwable> {
    /**
     * 执行方法
     *
     * @param source 参数
     * @return R
     * @throws E 异常消息
     */
    R invoke(T source) throws E;
}
