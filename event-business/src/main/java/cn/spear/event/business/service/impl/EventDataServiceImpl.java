package cn.spear.event.business.service.impl;

import cn.spear.event.business.dao.EventDataMapper;
import cn.spear.event.business.domain.po.EventDataPO;
import cn.spear.event.business.service.EventDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author luoyong
 * @date 2022/12/9
 */
@Service
public class EventDataServiceImpl extends ServiceImpl<EventDataMapper, EventDataPO> implements EventDataService {
    @Override
    public boolean deleteById(String id) {
        return getBaseMapper().removeById(id) > 0;
    }

    @Override
    public int batchDelete(Collection<String> ids) {
        return getBaseMapper().batchRemove(ids);
    }
}
