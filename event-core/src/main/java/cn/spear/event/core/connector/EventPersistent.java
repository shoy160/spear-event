package cn.spear.event.core.connector;

import io.cloudevents.CloudEvent;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author luoyong
 * @date 2022/12/9
 */
public interface EventPersistent {
    /**
     * 保存事件
     *
     * @param id         id
     * @param cloudEvent cloudEvent
     * @param sendAt     发送时间
     * @return boolean
     */
    boolean save(String id, CloudEvent cloudEvent, Date sendAt);

    /**
     * 保存事件
     *
     * @param id         事件 ID
     * @param cloudEvent 事件内容
     * @return boolean
     */
    default boolean save(String id, CloudEvent cloudEvent) {
        return save(id, cloudEvent, null);
    }

    /**
     * 获取事件
     *
     * @param id id
     * @return cloudEvent
     */
    CloudEvent get(String id);

    /**
     * 批量保存
     *
     * @param cloudEvents 事件列表
     * @return boolean
     */
    boolean batchSave(Map<String, CloudEvent> cloudEvents);

    /**
     * 批量获取
     *
     * @param ids id 列表
     * @return 事件
     */
    Map<String, CloudEvent> batchGet(Collection<String> ids);

    /**
     * 删除
     *
     * @param id id
     * @return boolean
     */
    boolean remove(String id);

    /**
     * 批量删除
     *
     * @param ids id 列表
     * @return boolean
     */
    boolean batchRemove(Collection<String> ids);
}
