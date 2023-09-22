package cn.spear.event.web.model.cmd;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author luoyong
 * @date 2023/4/23
 */
@Getter
@Setter
public class OptionsCmd {
    @NotNull(message = "配置值不能为空")
    @ApiModelProperty("配置值：1.可编排")
    private Integer options;

    @ApiModelProperty("是否为添加，默认为 true, false 为移除配置值")
    private Boolean add;

    public OptionsCmd() {
        this.add = true;
    }
}
