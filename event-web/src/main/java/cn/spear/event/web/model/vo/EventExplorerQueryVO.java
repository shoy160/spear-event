package cn.spear.event.web.model.vo;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author songxueyan
 * @date 2023/4/18
 */
@Getter
@Setter
@ToString
public class EventExplorerQueryVO {
    @ApiModelProperty(value = "事件类型")
    private EventTypeEnum type;
    @ApiModelProperty(value = "应用标识")
    private String appCode;
    @ApiModelProperty(value = "标签", required = true)
    private String[] tags;

    public EventExplorerQueryVO() {
        this.type = EventTypeEnum.Public;
        this.appCode = "";
    }

}
