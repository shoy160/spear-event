package cn.spear.event.web.model.cmd;

import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author luoyong
 * @date 2022/11/11
 */
@Getter
@Setter
public class ClientCmd extends BaseDTO {
    @ApiModelProperty("客户端 ID")
    private Long clientId;
    @ApiModelProperty("客户端密钥")
    private String clientSecret;
    
    @NotBlank(message = "客户端名称不能为空")
    @ApiModelProperty("客户端名称")
    private String name;
}
