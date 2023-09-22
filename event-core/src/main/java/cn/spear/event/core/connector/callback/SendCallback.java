package cn.spear.event.core.connector.callback;

import cn.spear.event.core.domain.event.SendResult;
import cn.spear.event.core.exception.ConnectorRuntimeException;

/**
 * 发送回调
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface SendCallback {
    /**
     * 成功处理
     *
     * @param sendResult 发送结果
     */
    void onSuccess(final SendResult sendResult);

    /**
     * 异常处理
     *
     * @param exception  异常
     * @param sendResult 发送结果
     */
    default void onException(final ConnectorRuntimeException exception, final SendResult sendResult) {
        
    }

    /**
     * 完成处理
     *
     * @param sendResult 发送结果
     */
    default void onCompleted(final SendResult sendResult) {
    }
}
