package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
@ToString
public class EventCreateDTO extends BaseDTO {
    /**
     * 事件类型
     */
    private EventTypeEnum type;
    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 私有 ID
     */
    private String privateId;
    private String code;
    private String name;
    private String desc;
    private Integer sort;
    private List<String> tags;
    private String topic;
    private Integer partition;
    private Integer retention;
}
