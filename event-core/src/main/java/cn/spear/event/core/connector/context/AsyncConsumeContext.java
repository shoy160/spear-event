package cn.spear.event.core.connector.context;

import cn.spear.event.core.enums.EventAction;

/**
 * 异步消费上下文
 *
 * @author luoyong
 * @date 2022/11/8
 */
public abstract class AsyncConsumeContext implements AbstractContext {

    /**
     * 提交事件
     *
     * @param action 事件操作
     */
    public abstract void commit(EventAction action);
}
