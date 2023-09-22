package cn.spear.event.web.model.vo;

import cn.spear.event.business.domain.enums.EventTypeEnum;
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
public class EventGroupQueryVO {
    @ApiModelProperty(value = "事件类型")
    private EventTypeEnum type;
    @ApiModelProperty(value = "私有 ID")
    private String privateId;
    @ApiModelProperty(value = "应用 & 场景")
    private String parent;
    @ApiModelProperty(value = "应用标识")
    private String appCode;
    @ApiModelProperty(value = "事件编码")
    private String code;
    @ApiModelProperty(value = "事件名称")
    private String name;
    @ApiModelProperty(value = "标签")
    private String tag;
    @ApiModelProperty(value = "配置项：1.可编排")
    private Integer options;
}
