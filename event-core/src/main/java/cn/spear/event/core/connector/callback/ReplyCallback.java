package cn.spear.event.core.connector.callback;

import io.cloudevents.CloudEvent;

/**
 * 回复回调
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface ReplyCallback {
    /**
     * 成功回调
     *
     * @param event 事件
     */
    void onSuccess(CloudEvent event);

    /**
     * 异常事件
     *
     * @param e 异常
     */
    void onException(Throwable e);
}
