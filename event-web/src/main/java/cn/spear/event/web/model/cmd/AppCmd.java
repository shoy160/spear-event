package cn.spear.event.web.model.cmd;

import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class AppCmd extends BaseDTO {
    /**
     * 应用类型
     */
    @NotNull(message = "应用类型不能为空")
    @ApiModelProperty("应用类型")
    @ApiParam(required = true)
    private AppTypeEnum type;

    /**
     * 唯一标志，如 myapp，这个应用下的所有事件必须为 myapp.xxx.xxx
     */
    @NotBlank(message = "应用 code 不能为空")
    @ApiModelProperty("应用 code")
    @ApiParam(required = true)
    private String code;

    /**
     * 名称
     */
    @NotBlank(message = "应用名称不能为空")
    @ApiModelProperty("应用名称")
    @ApiParam(required = true)
    private String name;

    /**
     * logo
     */
    @ApiModelProperty("应用 logo")
    private String logo;

    /**
     * 私有 ID
     */
    @ApiModelProperty("私有 ID")
    private String privateId;

    @ApiModelProperty("排序值")
    private Integer sort;

}
