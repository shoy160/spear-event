package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author luoyong
 * @date 2023/2/16
 */
@Getter
@Setter
@ToString
public class GroupQueryDTO extends BaseDTO {

    /**
     * 分组类型
     */
    private GroupTypeEnum type;

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 应用标识
     */
    private String appCode;

    /**
     * 父级 ID
     */
    private Long parentId;

    /**
     * 名称
     */
    private String name;

    private SortWayEnum sortWay;
}
