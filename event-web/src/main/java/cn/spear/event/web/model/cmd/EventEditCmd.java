package cn.spear.event.web.model.cmd;

import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class EventEditCmd extends BaseDTO {
    @NotBlank(message = "事件名称不能为空")
    @ApiModelProperty("事件名称")
    private String name;
    @ApiModelProperty("事件描述")
    private String desc;

    @ApiModelProperty("排序值")
    private Integer sort;
    @ApiModelProperty("事件标签列表")
    private List<String> tags;
}
