package cn.spear.event.core.connector;

import cn.spear.event.core.connector.context.AbstractContext;
import io.cloudevents.CloudEvent;

import java.util.List;
import java.util.Properties;

/**
 * 消费者接口
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface Consumer extends LifeCycle {
    /**
     * 初始化
     *
     * @param keyValue 配置
     * @throws Exception 异常
     */
    void init(Properties keyValue) throws Exception;

    /**
     * 更新游标
     *
     * @param cloudEvents 事件列表
     * @param context     上下文
     */
    void updateOffset(List<CloudEvent> cloudEvents, AbstractContext context);

    /**
     * 订阅
     *
     * @param topic Topic
     * @throws Exception Exception
     */
    void subscribe(String topic) throws Exception;

    /**
     * 取消订阅
     *
     * @param topic Topic
     */
    void unsubscribe(String topic);

    /**
     * 注册监听器
     *
     * @param listener 事件监听器
     */
    void registerEventListener(EventListener listener);
}
