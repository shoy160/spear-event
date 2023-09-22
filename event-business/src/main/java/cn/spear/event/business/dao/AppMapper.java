package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.AppPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;

/**
 * 应用仓储服务
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Repository
public interface AppMapper extends BaseMapper<AppPO> {
    /**
     * 查询应用
     *
     * @param code 应用编码
     * @return 应用
     */
    default AppPO queryByCode(String code) {
        return ChainWrappers.lambdaQueryChain(this)
                .eq(AppPO::getCode, code)
                .last("LIMIT 1")
                .one();
    }
}
