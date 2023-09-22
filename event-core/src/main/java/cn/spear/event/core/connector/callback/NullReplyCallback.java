package cn.spear.event.core.connector.callback;

import io.cloudevents.CloudEvent;

/**
 * @author luoyong
 * @date 2022/11/9
 */
public class NullReplyCallback implements ReplyCallback {
    private NullReplyCallback() {
    }

    public static NullReplyCallback instance() {
        return new NullReplyCallback();
    }

    @Override
    public void onSuccess(CloudEvent event) {

    }

    @Override
    public void onException(Throwable e) {

    }
}
