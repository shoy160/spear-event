package cn.spear.event.sdk.model;

import cn.spear.event.sdk.enums.eventEventType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author luoyong
 * @date 2023/2/9
 */
@Getter
@Setter
public class eventEvent {
    private Long id;
    /**
     * 事件类型
     */
    private eventEventType type;
    /**
     * 私有 ID
     */
    private String privateId;
    private String code;
    private String name;
    private String desc;
    private String topic;
    private List<String> tags;
    private Integer partition;
    private Integer replications;
    private Integer retention;
    private Long messageCount;
    private Date createdAt;
}
