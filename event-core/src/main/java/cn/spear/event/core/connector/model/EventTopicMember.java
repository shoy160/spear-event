package cn.spear.event.core.connector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/12/27
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventTopicMember {
    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 消费此Topic的成员数量
     */
    private Integer memberCount;
}
