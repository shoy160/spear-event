package cn.spear.event.web.model.cmd;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class EventCmd extends BaseDTO {
    @ApiModelProperty("事件类型")
    @NotNull(message = "事件类型不能为空")
    @ApiParam(required = true)
    private EventTypeEnum type;

    private String privateId;
    @ApiModelProperty("应用标识")
    private String appCode;


    @ApiModelProperty("排序值 默认值:0")
    private Integer sort;

    @NotBlank(message = "事件编码不能为空")
    @ApiModelProperty("事件编码")
    @ApiParam(required = true)
    private String code;

    @NotBlank(message = "事件名称不能为空")
    @ApiModelProperty("事件名称")
    @ApiParam(required = true)
    private String name;

    @ApiModelProperty("事件描述")
    private String desc;

    @ApiModelProperty("主题，默认为 code")
    private String topic;

    @ApiModelProperty("事件标签列表")
    private List<String> tags;

    @ApiModelProperty("分区数，默认：1")
    private Integer partition;

    @ApiModelProperty("数据有效期(天)，默认：30")
    private Integer retention;
    @ApiModelProperty("订阅分组（自定义事件）")
    private String consumerGroup;
    @ApiModelProperty("发布源（自定义事件）")
    private String producerSource;

    public EventCmd() {
        sort = 0;
        partition = 1;
        retention = 30;
    }
}
