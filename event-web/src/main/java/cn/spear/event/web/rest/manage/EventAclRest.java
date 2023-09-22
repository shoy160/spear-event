package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.EventDTO;
import cn.spear.event.business.domain.dto.EventPageDTO;
import cn.spear.event.business.domain.dto.EventQueryDTO;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.domain.po.ClientGrantPO;
import cn.spear.event.business.domain.po.ClientPO;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.service.ClientService;
import cn.spear.event.business.service.EventService;
import cn.spear.event.connector.kafka.KafkaAcl;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.model.EventAclBinding;
import cn.spear.event.core.connector.model.EventRecord;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.rest.BaseManageRest;
import cn.hutool.core.convert.Convert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
//@ApiIgnore
@RequiredArgsConstructor
@RestController("ManageEventAcl")
@RequestMapping("manage/event/acl")
@Api(value = "事件 ACL 管理", tags = "事件授权相关接口")
public class EventAclRest extends BaseManageRest {
    private final ConnectorAcl connectorAcl;
    private final KafkaAcl kafkaAcl;
    private final Cache<String, String> cache;

    private final EventService eventService;
    private final ClientService clientService;
    private final ClientGrantService grantService;

    @GetMapping("topic")
    @ApiOperation(value = "主题列表", notes = "主题列表")
    public List<String> topics() {
        return new ArrayList<>(connectorAcl.getTopicList());
    }

    @PostMapping("topic")
    @ApiOperation(value = "创建主题", notes = "创建主题")
    public int createTopic(@RequestBody String name) {
        return connectorAcl.createTopic(name, 1, 1, 30);
    }

    @DeleteMapping("topic")
    @ApiOperation(value = "删除主题", notes = "删除主题")
    public boolean deleteTopic(@RequestBody String name) {
        return connectorAcl.deleteTopic(name);
    }

    @PutMapping("topic")
    @ApiOperation(value = "分区扩容", notes = "分区扩容")
    public boolean extendTopic(@RequestParam String name, @RequestParam Integer partitions) {
        return connectorAcl.extendPartitions(name, partitions);
    }

    @PutMapping("cache")
    public String cacheTest(String key, String value) {
        cache.put(key, value, 10, TimeUnit.MINUTES);
        return key;
    }

    @GetMapping()
    public List<EventAclBinding> getAclList() {
        return connectorAcl.getAclList();
    }

    @DeleteMapping()
    public boolean cleanAcl() {
        List<EventAclBinding> bindings = connectorAcl.getAclList();
        for (EventAclBinding binding : bindings) {
            if ("5687494188339200".equals(binding.getUser())) {
                continue;
            }
            connectorAcl.deleteAcl(binding);
        }
        return true;
    }

    @PutMapping("sync/connector")
    public boolean syncConnector() {
        boolean result = syncClient();
        if (result) {
            result = syncEvent(false);
        }
        if (result) {
            result = syncGrant();
        }
        return result;
    }

    @PutMapping("sync/client")
    public boolean syncClient() {
        List<String> userList = connectorAcl.userList();
        List<ClientPO> clients = clientService.lambdaQuery()
                .eq(ClientPO::getStatus, StatusEnum.Enabled)
                .select(ClientPO::getId, ClientPO::getSecret)
                .list();
        // 同步数据库
        for (ClientPO client : clients) {
            String name = String.valueOf(client.getId());
            if (userList.contains(name)) {
                continue;
            }
            connectorAcl.createUser(name, client.getSecret());
        }
        // 清理无效的
        List<Long> clientIds = clients.stream().map(ClientPO::getId).collect(Collectors.toList());
        for (String name : userList) {
            Long id = Convert.toLong(name, 0L);
            if (Objects.isNull(id) || id <= 0) {
                continue;
            }
            if (!clientIds.contains(id)) {
                connectorAcl.deleteUser(name);
            }
        }
        return true;
    }

    @PutMapping("sync/event")
    public boolean syncEvent(boolean syncTopic) {
        syncEventByDb();
        if (syncTopic) {
            syncEventByTopic();
        }
        return true;
    }

    @PutMapping("sync/event/message")
    public boolean syncEventMessage() {
        eventService.syncMessageCount();
        return true;
    }

    @PutMapping("sync/grant")
    public boolean syncGrant() {
        List<ClientGrantPO> grantList = grantService.lambdaQuery()
                .eq(ClientGrantPO::getStatus, StatusEnum.Enabled)
                .list();
        for (ClientGrantPO grant : grantList) {
            EventAclBinding binding = grantService.createAclBinding(grant);
            connectorAcl.addAcl(binding);
        }
        return true;
    }

    @GetMapping("topic/message/count")
    public Map<String, Long> getMessageCount(
            @ApiParam(required = true, value = "主题列表，多个以,分割") @RequestParam() String topics
    ) {
        List<String> topicList = Arrays.stream(topics.split(",")).collect(Collectors.toList());
        return connectorAcl.getMessageCounts(topicList);
    }

    @GetMapping("topic/message")
    public PagedDTO<EventRecord> getRecords(String topic, int size) {
        return connectorAcl.getTopicRecords(size, topic, null, null);
    }

    private void syncEventByTopic() {
        log.info("开始同步 Topic 对应事件");
        Set<String> topicList = connectorAcl.getTopicList();
        for (String topic : topicList) {
            EventDTO event = eventService.findByCode(topic);
            if (Objects.isNull(event)) {
                connectorAcl.deleteTopic(topic);
            }
        }
    }

    private void syncEventByDb() {
        log.info("开始同步事件对应的 Topic");
        final int size = 20;
        int page = 1;
        Set<String> topicList = connectorAcl.getTopicList();
        while (true) {
            PagedDTO<EventPageDTO> paged = eventService.findByPaged(page, size, new EventQueryDTO());
            for (EventPageDTO event : paged.getList()) {
                String topic = event.getTopic();
                if (topicList.contains(topic)) {
                    continue;
                }
                connectorAcl.createTopic(topic, event.getPartition(), event.getReplications(), event.getRetention());
            }
            if (!paged.hasNext()) {
                break;
            }
            page++;
        }
    }
}
