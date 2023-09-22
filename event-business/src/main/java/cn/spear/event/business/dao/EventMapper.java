package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.core.exception.BusinessException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Repository
public interface EventMapper extends BaseMapper<EventPO> {
    /**
     * 查询事件
     *
     * @param code 事件编码
     * @return 事件 PO
     */
    default EventPO queryByCode(String code) {
        return ChainWrappers.lambdaQueryChain(this)
                .eq(EventPO::getCode, code)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 清理已删除的事件
     *
     * @param code 事件编码
     * @return rows count
     */
    @Delete("DELETE FROM event WHERE is_deleted=TRUE AND code=#{code}")
    int cleanByCode(@Param("code") String code);

    /**
     * 查询事件 （不存在则抛业务异常）
     *
     * @param eventCode 事件编码
     * @return 事件
     */
    default EventPO queryByCodeRequired(String eventCode) {
        EventPO event = queryByCode(eventCode);
        if (Objects.isNull(event)) {
            throw new BusinessException(String.format("事件[%s]不存在", eventCode));
        }
        return event;
    }

    /**
     * 通过事件 ID 或者事件编码查询
     *
     * @param id id
     * @return event
     */
    default EventPO queryById(Long id) {
        return ChainWrappers.lambdaQueryChain(this)
                .eq(EventPO::getId, id)
                .last("LIMIT 1")
                .one();
    }
}
