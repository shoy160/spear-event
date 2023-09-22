package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.EventDataPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 事件数据仓储
 *
 * @author luoyong
 * @date 2022/12/9
 */
@Repository
public interface EventDataMapper extends BaseMapper<EventDataPO> {
    /**
     * 删除事件数据
     *
     * @param id 数据 ID
     * @return int
     */
    @Delete("DELETE FROM event_data where id=#{id}")
    int removeById(@Param("id") String id);

    /**
     * 删除事件数据
     *
     * @param ids 数据 IDs
     * @return int
     */
    @Delete({
            "<script>",
            "DELETE FROM event_data where id IN",
            "<foreach item='item' collection='ids' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    int batchRemove(@Param("ids") Collection<String> ids);
}
