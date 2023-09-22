package cn.spear.event.business.domain.po;

import cn.spear.event.business.domain.enums.AuditOperationType;
import cn.spear.event.business.domain.enums.AuditResourceType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@TableName("audit_log")
@Getter
@Setter
public class AuditLogPO extends BasePO {

    @TableId
    private Long id;
    private AuditOperationType operationType;
    private AuditResourceType resourceType;
    private Long resourceId;
    private String desc;
    private String detail;
    private boolean result;
    private String userAgent;
    private String clientIp;
    private String creatorId;
    private String creatorName;
}
