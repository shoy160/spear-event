package cn.spear.event.web.model.vo;

import cn.spear.event.business.domain.enums.EventTypeEnum;
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
public class EventQueryVO extends PageVO {
    @ApiModelProperty(value = "事件类型")
    private EventTypeEnum type;

    @ApiModelProperty(value = "应用 & 场景")
    private String parent;
    @ApiModelProperty(value = "事件编码")
    private String code;
    @ApiModelProperty(value = "事件名称")
    private String name;
    @ApiModelProperty(value = "标签")
    private String tag;
    @ApiModelProperty(value = "应用编码")
    private String appCode;
    @ApiModelProperty(value = "排序方式")
    private SortWayEnum sortWay;
}
