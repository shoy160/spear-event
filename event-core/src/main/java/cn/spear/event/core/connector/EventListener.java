package cn.spear.event.core.connector;

import cn.spear.event.core.connector.context.AsyncConsumeContext;
import io.cloudevents.CloudEvent;

import java.util.List;

/**
 * 事件监听器
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface EventListener {
    /**
     * 是否批量消费
     *
     * @return boolean
     */
    boolean enableBatch();

    /**
     * 消费事件
     *
     * @param cloudEvent 事件
     * @param context    上下文
     */
    default void consume(final CloudEvent cloudEvent, final AsyncConsumeContext context) {
    }

    /**
     * 批量消费
     *
     * @param eventList 事件列表
     * @param context   上下文
     */
    default void batchConsume(final List<CloudEvent> eventList, final AsyncConsumeContext context) {
    }
}
