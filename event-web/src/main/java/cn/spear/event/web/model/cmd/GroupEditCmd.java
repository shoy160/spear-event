package cn.spear.event.web.model.cmd;

import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 事件分组/标签
 *
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
public class GroupEditCmd extends BaseDTO {

    @ApiModelProperty("事件名称")
    private String name;

    @ApiModelProperty("Logo")
    private String logo;

    @ApiModelProperty("排序")
    private Integer sort;
}
