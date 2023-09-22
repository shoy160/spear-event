package cn.spear.event.business.dao;

import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.po.ClientGrantPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Repository
public interface ClientGrantMapper extends BaseMapper<ClientGrantPO> {
    /**
     * 查询订阅授权
     *
     * @param clientId 客户端 ID
     * @param event    事件编码
     * @return grant
     */
    default ClientGrantPO queryConsumer(Long clientId, String event) {
        return ChainWrappers
                .lambdaQueryChain(this)
                .eq(ClientGrantPO::getClientId, clientId)
                .eq(ClientGrantPO::getType, GrantTypeEnum.Consumer)
                .eq(ClientGrantPO::getTopic, event)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 通过客户端 ID 授权列表查询
     *
     * @param clientId
     * @return grantIds
     */
    default List<Long> selectByClientId(Long clientId) {
        return ChainWrappers
                .lambdaQueryChain(this)
                .eq(ClientGrantPO::getClientId, clientId)
                .list().stream()
                .map(ClientGrantPO::getId)
                .collect(Collectors.toList());
    }
}
