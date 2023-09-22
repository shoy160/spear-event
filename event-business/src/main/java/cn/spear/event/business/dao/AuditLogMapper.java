package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.AuditLogPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Repository
public interface AuditLogMapper extends BaseMapper<AuditLogPO> {
}
