package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.AuditOperationType;
import cn.spear.event.business.domain.enums.AuditResourceType;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author leizeyu
 * @date 2023/2/28
 */
@Getter
@Setter
@ToString
public class AuditLogQueryDTO extends BaseDTO {

    @ApiModelProperty("操作类型")
    private AuditOperationType operationType;

    @ApiModelProperty("资源类型")
    private AuditResourceType resourceType;

    @ApiModelProperty("操作者 ID")
    private String operatorId;

    @ApiModelProperty("操作者")
    private String operatorName;

    @ApiModelProperty("操作结果")
    private Boolean result;

    private Date startTime;

    private Date endTime;
}
