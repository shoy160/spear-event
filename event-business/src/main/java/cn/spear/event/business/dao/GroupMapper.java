package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.GroupPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * 事件分组仓储
 *
 * @author luoyong
 * @date 2023/2/16
 */
@Repository
public interface GroupMapper extends BaseMapper<GroupPO> {
}
