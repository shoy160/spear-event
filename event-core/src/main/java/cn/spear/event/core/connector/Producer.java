package cn.spear.event.core.connector;

import cn.spear.event.core.connector.callback.NullSendCallback;
import cn.spear.event.core.connector.callback.ReplyCallback;
import cn.spear.event.core.connector.callback.SendCallback;
import io.cloudevents.CloudEvent;

import java.util.Properties;

/**
 * 生产者接口
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface Producer extends LifeCycle {
    /**
     * 初始化配置
     *
     * @param properties 配置属性
     * @throws Exception exception
     */
    void init(Properties properties) throws Exception;

    /**
     * 发布事件
     *
     * @param cloudEvent   事件
     * @param sendCallback 回调
     * @throws Exception exception
     */
    void publish(CloudEvent cloudEvent, SendCallback sendCallback);

    /**
     * 发布事件
     *
     * @param cloudEvent 事件
     * @throws Exception exception
     */
    default void publish(CloudEvent cloudEvent) {
        publish(cloudEvent, NullSendCallback.instance());
    }

    /**
     * 发送事件（单向）
     *
     * @param cloudEvent 事件
     */
    void sendOneway(final CloudEvent cloudEvent);

    /**
     * 请求
     *
     * @param cloudEvent 事件
     * @param rrCallback 回复回调
     * @param timeout    超时时间（秒）
     * @throws Exception exception
     */
    void request(CloudEvent cloudEvent, ReplyCallback rrCallback, long timeout);

    /**
     * 回复
     *
     * @param cloudEvent   事件
     * @param sendCallback 发送回调
     * @return boolean
     * @throws Exception exception
     */
    boolean reply(final CloudEvent cloudEvent, final SendCallback sendCallback);

    /**
     * 检测 Topic 是否存在
     *
     * @param topic Topic
     * @throws Exception exception
     */
    void checkTopicExist(String topic) throws Exception;

    /**
     * 设置扩展字段
     */
    void setExtFields();
}
