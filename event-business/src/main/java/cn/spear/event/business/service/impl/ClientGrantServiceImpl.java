package cn.spear.event.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.business.dao.ClientGrantMapper;
import cn.spear.event.business.dao.ClientMapper;
import cn.spear.event.business.dao.EventMapper;
import cn.spear.event.business.domain.dto.ClientBatchGrantDTO;
import cn.spear.event.business.domain.dto.ClientGrantDTO;
import cn.spear.event.business.domain.dto.EventDTO;
import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.*;
import cn.spear.event.business.domain.po.ClientGrantPO;
import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.Constants;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.model.EventAclBinding;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.EnumUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/12/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientGrantServiceImpl
        extends ServiceImpl<ClientGrantMapper, ClientGrantPO> implements ClientGrantService {
    private final Snowflake snowflake;
    private final ConnectorAcl connectorAcl;
    private final Cache<String, List<GrantDTO>> grantCache;

    private final ClientMapper clientMapper;
    private final EventMapper eventMapper;

    @Override
    public List<ClientGrantDTO> getList(Long clientId, GrantTypeEnum type, StatusEnum status) {
        return lambdaQuery()
                .eq(CommonUtils.isNotEmpty(clientId), ClientGrantPO::getClientId, clientId)
                .eq(null != type, ClientGrantPO::getType, type)
                .eq(null != status, ClientGrantPO::getStatus, status)
                .list()
                .stream()
                .map(t -> CommonUtils.toBean(t, ClientGrantDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean batchCreateGrant(Long clientId, GrantDTO grantDTO, List<String> topics) {
        if (CollUtil.isEmpty(topics)) {
            return true;
        }
        String errorMsg = "";
        for (String topic : topics) {
            try {
                grantDTO.setTopic(topic);
                createGrant(clientId, grantDTO);
            } catch (BusinessException ex) {
                errorMsg = errorMsg.concat(String.format("事件[%s]授权失败,失败原因[%s]\n", topic, ex.getMessage()));
            }
        }
        if (StrUtil.isNotBlank(errorMsg)) {
            throw new BusinessException(errorMsg);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGrant(Long clientId, GrantDTO grantDTO) {
        String eventCode = grantDTO.getTopic();
        if (StrUtil.isBlank(eventCode)) {
            throw new BusinessException("授权事件不能为空");
        }
        if (ResourcePatternEnum.Literal.equals(grantDTO.getPatternType())) {
            //精确模式
            eventMapper.queryByCodeRequired(eventCode);
        }
        boolean isExists = existsGrant(clientId, grantDTO);
        if (isExists) {
            String message =
                    String.format("客户端[%d]分组[%s]在事件[%s]上的[%s]授权已存在", clientId,
                            grantDTO.getGroup(), eventCode, grantDTO.getType().getName()
                    );
            throw new BusinessException(message);
        }
        ClientGrantPO model = CommonUtils.toBean(grantDTO, ClientGrantPO.class);
        model.setId(snowflake.nextId());
        model.setClientId(clientId);
        OperationTypeEnum operationType =
                GrantTypeEnum.Producer.equals(grantDTO.getType())
                        ? OperationTypeEnum.WRITE
                        : OperationTypeEnum.READ;
        model.setOperation(operationType);
        model.setStatus(StatusEnum.Enabled);
        boolean grantResult = save(model);
        if (grantResult) {
            EventAclBinding aclBinding = createAclBinding(model);
            connectorAcl.addAcl(aclBinding);
            updateClientGrantCache(clientId);
        }
        return grantResult;
    }

    @Override
    public boolean updateGrant(Long grantId, OperationTypeEnum operationType) {
        ClientGrantPO grantModel = findByRequired(grantId);
        boolean updateResult = lambdaUpdate()
                .eq(ClientGrantPO::getId, grantId)
                .set(ClientGrantPO::getOperation, operationType)
                .update();
        if (updateResult) {
            EventAclBinding aclBinding = createAclBinding(grantModel);
            connectorAcl.addAcl(aclBinding);
            updateClientGrantCache(grantModel.getClientId());
        }
        return updateResult;
    }

    @Override
    public boolean updateGrantStatus(Long grantId, StatusEnum status) {
        ClientGrantPO grantModel = findByRequired(grantId);
        boolean updateResult = lambdaUpdate()
                .eq(ClientGrantPO::getId, grantId)
                .set(ClientGrantPO::getStatus, status)
                .update();
        if (updateResult) {
            EventAclBinding aclBinding = createAclBinding(grantModel);
            if (StatusEnum.Enabled.equals(status)) {
                connectorAcl.addAcl(aclBinding);
            } else {
                connectorAcl.deleteAcl(aclBinding);
            }
            updateClientGrantCache(grantModel.getClientId());
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeGrant(Long grantId) {
        ClientGrantPO grantModel = findByRequired(grantId);
        boolean removeResult = lambdaUpdate()
                .eq(ClientGrantPO::getId, grantId)
                .remove();
        if (removeResult) {
            EventAclBinding aclBinding = createAclBinding(grantModel);
            connectorAcl.deleteAcl(aclBinding);
            updateClientGrantCache(grantModel.getClientId());
        }
        return removeResult;
    }

    @Override
    public PagedDTO<ClientGrantDTO> grantPaged(
            Long clientId, GrantTypeEnum grantType, ResourcePatternEnum patternType, StatusEnum status,
            int page, int size
    ) {
        Page<ClientGrantPO> grantPaged = lambdaQuery()
                .eq(null != clientId, ClientGrantPO::getClientId, clientId)
                .eq(null != grantType, ClientGrantPO::getType, grantType)
                .eq(null != patternType, ClientGrantPO::getPatternType, patternType)
                .eq(null != status, ClientGrantPO::getStatus, status)
                .orderByDesc(ClientGrantPO::getCreatedAt)
                .page(new Page<>(page, size));
        return PagedUtils.convert(grantPaged, ClientGrantDTO.class);
    }

    @Override
    public GrantDTO findGrant(Long clientId, GrantTypeEnum grantType, String eventCode, Object privateId) {
        EventPO event = eventMapper.queryByCodeRequired(eventCode);
        if (EventTypeEnum.Private.equals(event.getType())
                && !Objects.equals(event.getPrivateId(), privateId)) {
            throw new BusinessException("事件私有 ID 不匹配");
        }
        List<GrantDTO> grantList = getClientGrants(clientId, grantType);
        if (CollUtil.isEmpty(grantList)) {
            throw new BusinessException(String.format("客户端[%d]在事件[%s]上未授权[%s]", clientId, eventCode, grantType.getName()));
        }
        // 优先精确授权 -> 层级深的授权，如：spear.user. > spear.
        grantList.sort((a, b) -> {
            if (Objects.equals(a.getPatternType(), b.getPatternType())) {
                return Integer.compare(b.getTopic().length(), a.getTopic().length());
            }
            return Integer.compare(a.getPatternType().getValue(), b.getPatternType().getValue());
        });
//        log.info("grant list:{}", JsonUtils.toJson(grantList));
        for (GrantDTO grant : grantList) {
            if (grant.isMatch(grantType, eventCode)) {
                return grant;
            }
        }
        throw new BusinessException(String.format("客户端[%d]在事件[%s]上未授权[%s]", clientId, eventCode, grantType.getName()));
    }

    @Override
    public void removeByClientId(Long clientId) {
        List<ClientGrantPO> grants = lambdaQuery()
                .eq(ClientGrantPO::getClientId, clientId)
                .list();
        if (CollUtil.isEmpty(grants)) {
            return;
        }
        boolean removeResult = lambdaUpdate()
                .eq(ClientGrantPO::getClientId, clientId)
                .remove();
        if (removeResult) {
            List<EventAclBinding> bindings = grants.stream()
                    .map(this::createAclBinding).collect(Collectors.toList());
            connectorAcl.batchDeleteAcl(bindings);
            grantCache.remove(grantCacheKey(clientId));
        }
    }

    @Override
    public boolean existsGrant(Long clientId, GrantDTO grantDTO) {
        return lambdaQuery()
                .eq(ClientGrantPO::getClientId, clientId)
                .eq(ClientGrantPO::getType, grantDTO.getType())
                .eq(ClientGrantPO::getPatternType, grantDTO.getPatternType())
                .eq(ClientGrantPO::getTopic, grantDTO.getTopic())
                .eq(ClientGrantPO::getGroup, grantDTO.getGroup())
                .select(ClientGrantPO::getId)
                .exists();
    }

    @Override
    public void grantByClient(
            Long clientId, EventDTO eventDTO, String consumerGroup, String producerSource
    ) {
        if (StrUtil.isNotBlank(consumerGroup)) {
            grantProducerAndConsumer(eventDTO, clientId, consumerGroup, producerSource);
            return;
        }
        List<GrantDTO> grants = getClientGrants(clientId, GrantTypeEnum.Consumer);
        if (CollUtil.isEmpty(grants)) {
            GrantDTO grantDTO = new GrantDTO();
            grantDTO.setType(GrantTypeEnum.Producer);
            grantDTO.setPatternType(ResourcePatternEnum.Literal);
            grantDTO.setTopic(eventDTO.getTopic());
            grantDTO.setSource(producerSource);
            createGrant(clientId, grantDTO);
            return;
        }
        GrantDTO grant = grants.get(0);
        grantProducerAndConsumer(eventDTO, clientId, grant.getGroup(), grant.getSource());
    }

    @Override
    public boolean batchClientGrant(List<ClientBatchGrantDTO> source) {
        List<String> clientNames = source.stream()
                .map(ClientBatchGrantDTO::getClient)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Long> clientMap = clientMapper.queryByName(clientNames);
        for (ClientBatchGrantDTO client : source) {
            Long clientId = clientMap.get(client.getClient());
            List<ClientBatchGrantDTO.Grant> grants = client.getGrants();
            if (Objects.isNull(clientId) || CollUtil.isEmpty(grants)) {
                continue;
            }
            for (ClientBatchGrantDTO.Grant grant : grants) {
                batchCreateGrant(clientId
                        , GrantDTO.builder()
                                .patternType(grant.getPatternType())
                                .type(grant.getType())
                                .group(grant.getGroup())
                                .build()
                        , grant.getTopics());
            }
        }
        return true;
    }

    private void grantProducerAndConsumer(
            EventDTO event, Long clientId, String consumerGroup, String producerSource
    ) {
        GrantDTO grantDTO = new GrantDTO();
        grantDTO.setType(GrantTypeEnum.ProducerAndConsumer);
        if (EventTypeEnum.Private.equals(event.getType())) {
            // 私有事件按应用编码授权
            grantDTO.setPatternType(ResourcePatternEnum.Prefixed);
            grantDTO.setTopic(event.getAppCode().concat(Constants.STR_LEVEL));
        } else {
            grantDTO.setPatternType(ResourcePatternEnum.Literal);
            grantDTO.setTopic(event.getTopic());
        }
        grantDTO.setGroup(consumerGroup);
        grantDTO.setSource(producerSource);
        if (existsGrant(clientId, grantDTO)) {
            return;
        }
        createGrant(clientId, grantDTO);
    }

    private List<GrantDTO> getClientGrants(Long clientId, GrantTypeEnum grantType) {
        String cacheKey = grantCacheKey(clientId);
        List<GrantDTO> grantList = grantCache.get(cacheKey);
        // // TODO: 2023/3/27 多个环境共用一个 Redis 导致授权信息不一致
        if (CollUtil.isEmpty(grantList)) {
            grantList = updateClientGrantCache(clientId);
        }
        if (Objects.nonNull(grantType)) {
            return grantList.stream()
                    .filter(t -> EnumUtils.hasFlag(t.getType(), grantType))
                    .collect(Collectors.toList());
        }
        return grantList;
    }

    private List<GrantDTO> updateClientGrantCache(Long clientId) {
        List<GrantDTO> grantList = lambdaQuery()
                .eq(ClientGrantPO::getClientId, clientId)
                .eq(ClientGrantPO::getStatus, StatusEnum.Enabled)
                .list()
                .stream()
                .map(t -> CommonUtils.toBean(t, GrantDTO.class))
                .collect(Collectors.toList());
        String cacheKey = grantCacheKey(clientId);
        grantCache.put(cacheKey, grantList, 20, TimeUnit.MINUTES);
        return grantList;
    }

    private ClientGrantPO findByRequired(Long grantId) {
        ClientGrantPO grantModel = getById(grantId);
        if (null == grantModel) {
            throw new BusinessException("授权信息不存在");
        }
        return grantModel;
    }

    private static String grantCacheKey(Long clientId) {
        return String.format("client-grants:%d", clientId);
    }
}
