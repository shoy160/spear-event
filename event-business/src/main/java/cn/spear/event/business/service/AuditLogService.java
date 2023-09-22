package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.AuditLogDTO;
import cn.spear.event.business.domain.dto.AuditLogQueryDTO;
import cn.spear.event.business.domain.po.AuditLogPO;
import cn.spear.event.core.domain.dto.PagedDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
public interface AuditLogService extends IService<AuditLogPO> {

    /**
     *  保存审计记录
     * @param auditLog log
     */
    void saveRecord(AuditLogDTO auditLog);

    /**
     *  审计分页查询
     * @param query query
     * @param page page
     * @param size size
     * @return page
     */
    PagedDTO<AuditLogDTO> findPage(AuditLogQueryDTO query, Integer page, Integer size);
}
