package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.ClientBatchGrantDTO;
import cn.spear.event.business.domain.dto.ClientGrantDTO;
import cn.spear.event.business.domain.dto.EventDTO;
import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.OperationTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.domain.po.ClientGrantPO;
import cn.spear.event.core.connector.enums.EventAclOperation;
import cn.spear.event.core.connector.enums.EventGrantType;
import cn.spear.event.core.connector.enums.EventPatternType;
import cn.spear.event.core.connector.model.EventAclBinding;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.EnumUtils;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 客户端授权服务
 *
 * @author luoyong
 * @date 2022/12/2
 */
public interface ClientGrantService extends IService<ClientGrantPO> {
    /**
     * 授权列表
     *
     * @param clientId 客户端 ID
     * @param type     授权类型
     * @param status   状态
     * @return List of ClientGrantDTO
     */
    List<ClientGrantDTO> getList(Long clientId, GrantTypeEnum type, StatusEnum status);

    /**
     *  批量授权
     * @param clientId clientId
     * @param grantDTO grantDTO
     * @param topics topics
     * @return
     */
    boolean batchCreateGrant(Long clientId, GrantDTO grantDTO, List<String> topics);

    /**
     * 授权
     *
     * @param clientId 客户端 ID
     * @param grantDTO 授权实体
     * @return boolean
     */
    boolean createGrant(Long clientId, GrantDTO grantDTO);

    /**
     * 授权
     *
     * @param grantId       授权 ID
     * @param operationType 授权操作
     * @return boolean
     */
    boolean updateGrant(Long grantId, OperationTypeEnum operationType);

    /**
     * 更新授权状态
     *
     * @param grantId 授权 ID
     * @param status  授权状态
     * @return boolean
     */
    boolean updateGrantStatus(Long grantId, StatusEnum status);

    /**
     * 授权
     *
     * @param grantId 授权 ID
     * @return boolean
     */
    boolean removeGrant(Long grantId);

    /**
     * 查询授权
     *
     * @param clientId    客户端 ID
     * @param grantType   授权类型
     * @param patternType 匹配模式
     * @param status      授权状态
     * @param page        page
     * @param size        size
     * @return 授权列表
     */
    PagedDTO<ClientGrantDTO> grantPaged(
            Long clientId, GrantTypeEnum grantType, ResourcePatternEnum patternType, StatusEnum status,
            int page, int size
    );

    /**
     * 查询授权
     *
     * @param clientId  客户端 ID
     * @param grantType 授权类型
     * @param eventCode 事件编码
     * @param privateId 私有 ID
     * @return 授权
     */
    GrantDTO findGrant(Long clientId, GrantTypeEnum grantType, String eventCode, Object privateId);

    /**
     * 删除客户端授权
     *
     * @param clientId 客户端 ID
     */
    void removeByClientId(Long clientId);

    /**
     * 是否存在授权
     *
     * @param clientId 客户端 ID
     * @param grantDTO 授权实体
     * @return 是否存在授权
     */
    boolean existsGrant(Long clientId, GrantDTO grantDTO);

    /**
     * 客户端授权
     *
     * @param clientId       客户端 ID
     * @param eventDTO       事件
     * @param consumerGroup  订阅分组（自定义事件）
     * @param providerSource 发布源（自定义事件）
     */
    void grantByClient(Long clientId, EventDTO eventDTO, String consumerGroup, String providerSource);



    /**
     *  批量多客户端授权
     * @param source source
     * @return boolean
     */
    boolean batchClientGrant(List<ClientBatchGrantDTO> source);

    /**
     * 创建 eventBinding
     *
     * @param grant 授权实体
     * @return eventAclBinding
     */
    default EventAclBinding createAclBinding(ClientGrantPO grant) {
        EventGrantType grantType = EnumUtils.convertByName(grant.getType(), EventGrantType.class);
        EventPatternType patternType = EnumUtils.convertByName(grant.getPatternType(), EventPatternType.class, EventPatternType.Literal);
        EventAclOperation operation = EnumUtils.convertByName(grant.getOperation(), EventAclOperation.class);
        EventAclBinding aclBinding = CommonUtils.toBean(grant, EventAclBinding.class);
        aclBinding.setUser(String.valueOf(grant.getClientId()));
        aclBinding.setGrantType(grantType);
        aclBinding.setPatternType(patternType);
        aclBinding.setOperation(operation);
        return aclBinding;
    }

}
