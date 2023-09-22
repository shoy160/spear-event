package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
public class EventDTO extends BaseDTO {
    private Long id;
    /**
     * 事件类型
     */
    @ApiModelProperty("事件类型")
    private EventTypeEnum type;
    /**
     * 私有 ID
     */
    @ApiModelProperty("私有 ID")
    private String privateId;

    /**
     * 应用编码
     */
    @ApiModelProperty("应用编码")
    private String appCode;
    @ApiModelProperty("事件编码")
    private String code;
    @ApiModelProperty("事件名称")
    private String name;
    @ApiModelProperty("事件描述")
    private String desc;
    @ApiModelProperty("排序值")
    private Integer sort;
    /**
     * 配置项
     */
    @ApiModelProperty("配置项")
    private Integer options;
    @ApiModelProperty("事件主题")
    private String topic;
    @ApiModelProperty("事件标签")
    private List<String> tags;
    @ApiModelProperty("分区数")
    private Integer partition;
    @ApiModelProperty("副本数")
    private Integer replications;
    @ApiModelProperty("数据有效期(天)")
    private Integer retention;
    @ApiModelProperty("消息数量")
    private Long messageCount;
    @ApiModelProperty("创建时间")
    private Date createdAt;

    public String getTopic() {
        return Optional.ofNullable(topic).orElse(code);
    }

    public EventDTO() {
        this.tags = new ArrayList<>();
    }
}