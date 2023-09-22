package cn.spear.event.business.service.impl;

import cn.spear.event.business.dao.AuditLogMapper;
import cn.spear.event.business.domain.dto.AuditLogDTO;
import cn.spear.event.business.domain.dto.AuditLogQueryDTO;
import cn.spear.event.business.domain.po.AuditLogPO;
import cn.spear.event.business.service.AuditLogService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.utils.JsonUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLogPO> implements AuditLogService {

    private final Snowflake snowflake;


    @Override
    public void saveRecord(AuditLogDTO auditLog) {
        auditLog.setId(snowflake.nextId());
        AuditLogPO source = auditLog.convertPO();
        save(source);
    }

    @Override
    public PagedDTO<AuditLogDTO> findPage(AuditLogQueryDTO query, Integer page, Integer size) {
        Page<AuditLogPO> pages = lambdaQuery()
                .eq(Objects.nonNull(query.getResourceType()), AuditLogPO::getResourceType, query.getResourceType())
                .eq(Objects.nonNull(query.getOperationType()), AuditLogPO::getOperationType, query.getOperationType())
                .eq(Objects.nonNull(query.getResult()), AuditLogPO::isResult, query.getResult())
                .eq(StrUtil.isNotBlank(query.getOperatorId()), AuditLogPO::getCreatorId, query.getOperatorId())
                .like(StrUtil.isNotBlank(query.getOperatorName()), AuditLogPO::getCreatorName, query.getOperatorName())
                .ge(Objects.nonNull(query.getStartTime()), AuditLogPO::getCreatedAt, query.getStartTime())
                .le(Objects.nonNull(query.getEndTime()), AuditLogPO::getCreatedAt, query.getEndTime())
                .page(new Page<>(page, size));
        return PagedUtils.convert(pages, i -> {
            CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().ignoreError();
            AuditLogDTO source = BeanUtil.toBean(i, AuditLogDTO.class, copyOptions);
            if (StrUtil.isNotBlank(i.getDetail())) {
                source.setDetail(JsonUtils.jsonList(i.getDetail(),AuditLogDTO.ChangeField.class));
            }
            return source;
        });
    }
}
