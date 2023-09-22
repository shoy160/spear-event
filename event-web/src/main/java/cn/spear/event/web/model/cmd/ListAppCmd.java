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
public class ListAppCmd extends BaseDTO {
    /**
     * 租户 ID
     */
    @NotBlank(message = "应用 privateId 不能为空")
    @ApiModelProperty("应用 privateId")
    @ApiParam(required = true)
    private String privateId;
}
