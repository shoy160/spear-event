package cn.spear.event.web.model.vo;

import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import io.swagger.annotations.ApiModelProperty;
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
public class GroupQueryVO extends PageVO {
    @ApiModelProperty(value = "事件类型")
    private GroupTypeEnum type;
    @ApiModelProperty(value = "应用 ID")
    private Long appId;
    @ApiModelProperty(value = "应用标识")
    private String appCode;
    @ApiModelProperty(value = "父级 ID")
    private Long parentId;
    @ApiModelProperty(value = "事件名称")
    private String name;
    @ApiModelProperty(value = "排序值排序")
    private SortWayEnum sortWay;
}
