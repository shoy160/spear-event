package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.AuditOperationType;
import cn.spear.event.business.domain.enums.AuditResourceType;
import cn.spear.event.business.domain.po.AuditLogPO;
import cn.spear.event.core.utils.JsonUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;

    private String creatorId;

    private String creatorName;

    private String clientIp;

    private AuditResourceType resourceType;

    private AuditOperationType operationType;

    private String desc;

    private List<ChangeField> detail;

    private Boolean result;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeField {
        private String fieldName;
        private Object oldValue;
        private Object newValue;
    }

    public AuditLogDTO() {
        this.detail = new ArrayList<>();
    }

    public void recordChange(String fieldName, Object oldValue, Object newValue) {
        if (null == this.detail) {
            this.detail = new ArrayList<>();
        }
        this.detail.add(ChangeField.builder()
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build()
        );
    }

    public AuditLogPO convertPO() {
        CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().ignoreError();
        AuditLogPO auditLog = BeanUtil.toBean(this, AuditLogPO.class, copyOptions);
        if (CollUtil.isNotEmpty(this.detail)) {
            auditLog.setDetail(JsonUtils.toJson(this.detail));
        }
        return auditLog;
    }
}
