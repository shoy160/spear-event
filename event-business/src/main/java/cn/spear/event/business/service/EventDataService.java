package cn.spear.event.business.service;

import cn.spear.event.business.domain.po.EventDataPO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

/**
 * @author luoyong
 * @date 2022/12/9
 */
public interface EventDataService extends IService<EventDataPO> {
    /**
     * 删除数据
     *
     * @param id 数据 ID
     * @return boolean
     */
    boolean deleteById(String id);

    /**
     * 批量删除数据
     *
     * @param ids 数据 IDs
     * @return int
     */
    int batchDelete(Collection<String> ids);
}
