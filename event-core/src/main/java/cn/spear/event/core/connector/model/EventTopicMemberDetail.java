package cn.spear.event.core.connector.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/12/28
 */
@Getter
@Setter
public class EventTopicMemberDetail {
    /**
     * 主题名称
     */
    private String topicName;
    /**
     * 分区 ID
     */
    private Integer partitionId;
    /**
     * Member ID
     */
    private String memberId;
    /**
     * Client ID
     */
    private String clientId;
    /**
     * Host
     */
    private String host;
    /**
     * 当前 Offset
     */
    private Long currentOffset;
    /**
     * 最新 Offset
     */
    private Long logEndOffset;
    /**
     * 偏移量
     */
    private Long lag;
}
