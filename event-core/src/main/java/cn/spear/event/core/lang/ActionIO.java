package cn.spear.event.core.lang;

import java.io.IOException;

/**
 * Action IO异常
 *
 * @author luoyong
 * @date 2021/9/23
 */
public interface ActionIO<T> {
    /**
     * 执行操作
     *
     * @param source source
     * @throws Exception ex
     */
    void invoke(T source) throws Exception;
}
