package cn.spear.event.business.dao;

import cn.spear.event.business.domain.dto.EventSchemaDTO;
import cn.spear.event.business.domain.po.EventSchemaPO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author leizeyu
 * @date 2023/2/24
 */
@Repository
public interface EventSchemaMapper extends BaseMapper<EventSchemaPO> {


    default EventSchemaDTO getSchemaById(Long id) {
        EventSchemaPO source = ChainWrappers.lambdaQueryChain(this)
                .eq(Objects.nonNull(id), EventSchemaPO::getId, id)
                .last("limit 1")
                .one();
        return BeanUtil.toBean(source, EventSchemaDTO.class);
    }

    default EventSchemaDTO getSchemaByCode(String code) {
        EventSchemaPO source = ChainWrappers.lambdaQueryChain(this)
                .eq(StrUtil.isNotBlank(code), EventSchemaPO::getCode, code)
                .last("limit 1")
                .one();
        return BeanUtil.toBean(source, EventSchemaDTO.class);
    }

    default boolean saveOrUpdate(EventSchemaPO target) {
        EventSchemaPO source = selectById(target.getId());
        return 1 <= (Objects.isNull(source) ?
                insert(target) :
                updateById(target));
    }
}
