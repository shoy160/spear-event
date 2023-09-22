package cn.spear.event.business.dao;

import cn.spear.event.business.domain.po.ClientPO;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Repository
public interface ClientMapper extends BaseMapper<ClientPO> {

    default Map<String, Long> queryByName(List<String> clientNames) {
        if (CollUtil.isEmpty(clientNames)){
            return new HashMap<>(0);
        }
        return ChainWrappers.lambdaQueryChain(this)
                .in(ClientPO::getName, clientNames)
                .select(ClientPO::getId, ClientPO::getName)
                .list().stream()
                .collect(Collectors.toMap(ClientPO::getName, ClientPO::getId));
    }
}
