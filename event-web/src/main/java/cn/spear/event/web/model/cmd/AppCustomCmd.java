package cn.spear.event.web.model.cmd;

import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class AppCustomCmd extends BaseDTO {
    /**
     * 唯一标志，如 myapp，这个应用下的所有事件必须为 myapp.xxx.xxx
     */
    @NotBlank(message = "应用 identifier 不能为空")
    @ApiModelProperty("应用 identifier")
    @ApiParam(required = true)
    private String identifier;

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
    @NotBlank(message = "应用 logo 不能为空")
    @ApiModelProperty("应用 logo")
    @ApiParam(required = true)
    private String logo;

    /**
     * 租户 ID
     */
    @NotBlank(message = "应用 privateId 不能为空")
    @ApiModelProperty("应用 privateId")
    @ApiParam(required = true)
    private String privateId;
}
