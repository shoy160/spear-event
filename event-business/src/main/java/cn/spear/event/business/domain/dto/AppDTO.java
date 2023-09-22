package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 事件传输实体
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
@ToString
@ApiModel
public class AppDTO extends BaseDTO {

    private Long id;

    @ApiModelProperty("应用类型")
    private AppTypeEnum type;
    /**
     * 唯一标志，如 myapp，这个应用下的所有事件必须为 myapp.xxx.xxx
     */

    @ApiModelProperty("应用编码")
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty("应用名称")
    private String name;

    /**
     * logo
     */
    @ApiModelProperty("应用 Logo")
    private String logo;

    /**
     * 租户 ID
     */
    @ApiModelProperty("私有 ID")
    private String privateId;

    @ApiModelProperty("排序值")
    private Integer sort;

    /**
     * 配置项
     */
    @ApiModelProperty("配置项")
    private Integer options;

    @ApiModelProperty("创建事件")
    private Date createdAt;
}
