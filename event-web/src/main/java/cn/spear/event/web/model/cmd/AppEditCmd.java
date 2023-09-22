package cn.spear.event.web.model.cmd;

import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Getter
@Setter
@ToString
public class AppEditCmd extends BaseDTO {

    /**
     * 名称
     */
    @ApiModelProperty("应用名称")
    @NotBlank(message = "应用名称不能为空")
    private String name;

    /**
     * logo
     */
    @ApiModelProperty("应用 logo")
    private String logo;

    @ApiModelProperty("排序值")
    private Integer sort;

}
