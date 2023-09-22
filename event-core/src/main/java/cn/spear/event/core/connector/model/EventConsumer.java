package cn.spear.event.core.connector.model;

import cn.spear.event.core.connector.enums.ConsumerState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author luoyong
 * @date 2022/12/27
 */
@Getter
@Setter
public class EventConsumer {
    private String groupId;
    
    private boolean isSimple;
    /**
     * 状态
     */
    private ConsumerState state;
    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 消费的 topic 列表
     */
    private List<EventTopicMember> topicMembers;

    /**
     * 分配策略
     */
    private String partitionAssignor;

    /**
     * 协调器 brokerId
     */
    private int coordinatorId;
}
