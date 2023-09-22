package cn.spear.event.core.connector.callback;

import cn.spear.event.core.domain.event.SendResult;
import cn.spear.event.core.exception.ConnectorRuntimeException;

/**
 * @author luoyong
 * @date 2022/11/9
 */
public class NullSendCallback implements SendCallback {
    private NullSendCallback() {
    }

    public static NullSendCallback instance() {
        return new NullSendCallback();
    }

    @Override
    public void onSuccess(SendResult sendResult) {
    }

    @Override
    public void onException(ConnectorRuntimeException exception, SendResult sendResult) {
    }

    @Override
    public void onCompleted(SendResult sendResult) {

    }
}
