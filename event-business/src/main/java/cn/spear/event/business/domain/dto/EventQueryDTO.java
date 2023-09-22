package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author luoyong
 * @date 2023/1/5
 */
@Getter
@Setter
@ToString
public class EventQueryDTO extends BaseDTO {
    private EventTypeEnum type;
    private String privateId;
    private String parent;
    private String appCode;
    private String code;
    private String name;
    private String tag;
    private Integer options;
    private Long clientId;
    private SortWayEnum sortWay;
}
