package cn.spear.event.web.model.cmd;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author sisa
 * @date 2022/12/13
 */
@Getter
@Setter
@ToString
public class ClientEditCmd {

    @NotBlank
    @ApiModelProperty("客户端名称")
    private String name;
}

