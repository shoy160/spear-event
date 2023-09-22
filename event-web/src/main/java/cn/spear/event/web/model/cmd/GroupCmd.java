package cn.spear.event.web.model.cmd;

import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 事件分组/标签
 *
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class GroupCmd extends BaseDTO {
    @ApiModelProperty("类型")
    @NotNull(message = "类型不能为空")
    @ApiParam(required = true)
    private GroupTypeEnum type;

    @ApiModelProperty("应用标识")
    @NotNull(message = "应用标识不能为空")
    @ApiParam(required = true)
    private String appCode;

    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    @ApiParam(required = true)
    private String name;

    @ApiModelProperty("Logo")
    private String logo;

    @ApiModelProperty("父级 ID")
    private Long parentId;

    @ApiModelProperty("排序")
    private Integer sort;
}
