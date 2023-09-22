package cn.spear.event.business.service.impl;

import cn.spear.event.business.dao.ClientGrantMapper;
import cn.spear.event.business.dao.ClientMapper;
import cn.spear.event.business.dao.EventMapper;
import cn.spear.event.business.domain.dto.ClientDTO;
import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.domain.po.ClientPO;
import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.service.ClientService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import cn.spear.event.core.utils.CommonUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl extends ServiceImpl<ClientMapper, ClientPO> implements ClientService {
    private final Snowflake snowflake;
    private final ConnectorAcl connectorAcl;
    private final ConnectorManager connectorManager;
    private final Cache<String, ClientDTO> clientCache;

    private final ClientGrantMapper grantMapper;
    private final EventMapper eventMapper;

    private final ClientGrantService grantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientDTO create(String name, Long clientId, String clientSecret) {
        ClientPO model = new ClientPO();
        if (Objects.isNull(clientId) || clientId <= 0) {
            clientId = snowflake.nextId();
        }
        model.setId(clientId);
        model.setName(name);
        if (StrUtil.isBlank(clientSecret)) {
            clientSecret = IdUtil.fastSimpleUUID();
        }
        model.setSecret(clientSecret);
        model.setStatus(StatusEnum.Enabled);
        boolean saveResult = save(model);
        if (saveResult) {
            String username = String.valueOf(model.getId());
            boolean aclResult = connectorAcl.createUser(username, model.getSecret());
            if (!aclResult) {
                throw new ConnectorRuntimeException("创建 ACL 用户失败");
            }
        }
        return CommonUtils.toBean(model, ClientDTO.class);
    }

    @Override
    public PagedDTO<ClientDTO> paged(int page, int size, String name) {
        Page<ClientPO> clientPaged = lambdaQuery()
                .like(StrUtil.isNotBlank(name), ClientPO::getName, name)
                .orderByDesc(ClientPO::getCreatedAt)
                .page(new Page<>(page, size));
        return PagedUtils.convert(clientPaged, ClientDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long clientId) {
        ClientPO model = getById(clientId);
        if (Objects.isNull(model)) {
            throw new BusinessException("该客户端 ID 不存在");
        }
        // 删除客户端
        boolean result = removeById(clientId);
        if (result) {
            // 删除该客户端下所有授权
            grantService.removeByClientId(clientId);
            boolean aclResult = connectorAcl.deleteUser(clientId.toString());
            if (!aclResult) {
                throw new ConnectorRuntimeException("删除 ACL 用户失败");
            }
        }
        return result;
    }

    @Override
    public ClientDTO getDetail(Long id) {
        ClientPO model = getById(id);
        if (Objects.isNull(model)) {
            throw new BusinessException("客户端 ID 不存在");
        }
        return BeanUtil.toBean(model, ClientDTO.class, CopyOptions.create().ignoreError().ignoreNullValue());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSecret(Long clientId) {
        ClientPO model = getById(clientId);
        if (null == model) {
            throw new BusinessException("客户端不存在");
        }
        String secret = IdUtil.fastSimpleUUID();
        boolean result = lambdaUpdate().eq(ClientPO::getId, clientId)
                .set(ClientPO::getSecret, secret)
                .update();
        if (result) {
            String username = String.valueOf(model.getId());
            boolean aclAdd = connectorAcl.updateUser(username, secret);
            if (!aclAdd) {
                throw new BusinessException("更新客户端密钥失败");
            }
            clientCache.remove(clientCacheKey(model.getId()));
        }
        return result;
    }


    @Override
    public ClientDTO findById(Long clientId) {
        String cacheKey = clientCacheKey(clientId);
        return clientCache.getOrPut(cacheKey, key -> {
            ClientPO model = getById(clientId);
            return CommonUtils.toBean(model, ClientDTO.class);
        }, 7, TimeUnit.HOURS);
    }

    @Override
    public ClientDTO verify(Long clientId, String secret) {
        ClientDTO client = findById(clientId);
        if (null == client) {
            throw new BusinessException(String.format("客户端[%d]不存在", clientId));
        }
        if (!StrUtil.equalsIgnoreCase(secret, client.getSecret())) {
            throw new BusinessException(String.format("客户端[%d]验证失败", clientId));
        }
        if (!StatusEnum.Enabled.equals(client.getStatus())) {
            throw new BusinessException("客户端已被禁用");
        }
        return CommonUtils.toBean(client, ClientDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long clientId, StatusEnum status) {
        ClientPO model = getById(clientId);
        if (null == model) {
            throw new BusinessException("客户端不存在");
        }
        if (Objects.equals(model.getStatus(), status)) {
            return true;
        }
        boolean updateResult = lambdaUpdate()
                .eq(ClientPO::getId, clientId)
                .set(ClientPO::getStatus, status)
                .update();
        if (updateResult) {
            boolean aclResult;
            String username = String.valueOf(model.getId());
            if (StatusEnum.Enabled.equals(status)) {
                aclResult = connectorAcl.createUser(username, model.getSecret());
            } else {
                aclResult = connectorAcl.deleteUser(username);
            }
            if (!aclResult) {
                throw new ConnectorRuntimeException("更新 ACL 用户失败");
            }
        }
        return false;
    }

    @Override
    public Map<String, String> consumerConfig(Long clientId, String event, LanguageType languageType, String privateId) {
        ClientDTO client = findById(clientId);
        if (null == client) {
            throw new BusinessException("客户端不存在");
        }
        if (!StatusEnum.Enabled.equals(client.getStatus())) {
            throw new BusinessException("客户端未启用");
        }
        EventPO eventModel = eventMapper.queryByCode(event);
        if (null == eventModel) {
            throw new BusinessException(String.format("事件[%s]不存在", event));
        }
        GrantDTO grant = grantService.findGrant(clientId, GrantTypeEnum.Consumer, event, privateId);
        if (Objects.isNull(grant)) {
            throw new BusinessException(String.format("未获得事件[%s]的订阅权限", event));
        }

        String username = String.valueOf(clientId);
        Properties properties = connectorManager
                .createConsumerConfig(grant.getGroup(), languageType, username, client.getSecret());
        Map<String, String> config = new HashMap<>(properties.size());
        for (String name : properties.stringPropertyNames()) {
            config.put(name, properties.getProperty(name));
        }
        return config;
    }

    @Override
    public boolean edit(Long clientId, String name) {
        ClientPO client = getById(clientId);
        if (null == client) {
            throw new BusinessException("客户端不存在");
        }
        return lambdaUpdate().eq(ClientPO::getId, clientId)
                .set(CharSequenceUtil.isNotBlank(name), ClientPO::getName, name)
                .update();
    }

    private static String clientCacheKey(Long clientId) {
        return String.format("client:%d", clientId);
    }
}
