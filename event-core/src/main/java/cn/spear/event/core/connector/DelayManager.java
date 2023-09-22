package cn.spear.event.core.connector;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import io.cloudevents.CloudEvent;
import lombok.NonNull;

import java.util.Date;

/**
 * @author luoyong
 * @date 2022/12/8
 */
public interface DelayManager extends LifeCycle {
    /**
     * 发送延迟消息
     *
     * @param cloudEvent   事件
     * @param delayMinutes 延迟时间（分钟）
     */
    default void sendDelay(@NonNull CloudEvent cloudEvent, int delayMinutes) {
        DateTime delayAt = DateUtil.offsetMinute(DateUtil.date(), delayMinutes);
        sendDelay(cloudEvent, delayAt);
    }

    /**
     * 发送延迟消息
     *
     * @param cloudEvent 事件
     * @param delayAt    延迟发送时间
     */
    void sendDelay(@NonNull CloudEvent cloudEvent, Date delayAt);
}
