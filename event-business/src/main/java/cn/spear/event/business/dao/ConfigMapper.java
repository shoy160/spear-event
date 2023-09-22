package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.ConfigPO;
import cn.spear.event.core.exception.BusinessException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author leizeyu
 * @date 2023/3/6
 */
@Repository
public interface ConfigMapper extends BaseMapper<ConfigPO> {

    /**
     * 查询配置
     *
     * @param key     配置 KEY
     * @param profile 配置环境
     * @return Config Entity
     */
    default ConfigPO queryConfig(String key, String profile) {
        ConfigPO config = ChainWrappers.lambdaQueryChain(this)
                .eq(ConfigPO::getKey, key)
                .and(t -> t.isNull(ConfigPO::getProfile).or().eq(ConfigPO::getProfile, profile))
                .orderByDesc(ConfigPO::getProfile)
                .last("LIMIT 1")
                .one();
        if (Objects.isNull(config)) {
            throw new BusinessException("配置未找到");
        }
        return config;

    }
}
