package cn.spear.event.web.rest.app;

import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.service.ClientService;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.Consumer;
import cn.spear.event.core.connector.EventListener;
import cn.spear.event.core.connector.context.AsyncConsumeContext;
import cn.spear.event.core.connector.enums.ConsumerResetType;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.enums.EventAction;
import cn.spear.event.web.rest.BaseAppRest;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 事件中心
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Slf4j
@RequiredArgsConstructor
@RestController("EventSubApp")
@RequestMapping("app/event/sub")
@Api(value = "事件订阅", tags = "事件订阅接口")
public class EventSubRest extends BaseAppRest {
    private final ConnectorManager connectorManager;

    private final ClientGrantService grantService;
    private final ClientService clientService;

    @GetMapping("config")
    @ApiOperation(value = "获取订阅配置", notes = "获取订阅配置")
    public Map<String, String> getSubConfig(
            @RequestParam() @ApiParam(required = true, value = "事件编码") String event,
            @RequestParam(required = false) @ApiParam(value = "私有 ID") String privateId,
            @RequestParam(required = false) @ApiParam(value = "编程语言，默认 JAVA") LanguageType language) {
        language = Optional.ofNullable(language).orElse(LanguageType.Java);
        return clientService.consumerConfig(clientId(), event, language, privateId);
    }

    @ApiIgnore
    @PutMapping()
    @ApiOperation(value = "订阅事件", notes = "订阅事件")
    public void sub(String event, ConsumerResetType resetType, String privateId) throws Exception {
        GrantDTO grant = grantService.findGrant(clientId(), GrantTypeEnum.Consumer, event, privateId);
        Consumer consumer =
                connectorManager.createConsumer(grant.getGroup(), String.valueOf(clientId()), clientSecret(), resetType);
        consumer.registerEventListener(new EventListener() {
            @Override
            public boolean enableBatch() {
                return false;
            }

            @Override
            public void consume(CloudEvent cloudEvent, AsyncConsumeContext context) {
                CloudEventData data = cloudEvent.getData();
                if (null == data) {
                    return;
                }
                log.info("{} received: {}", cloudEvent.getSubject(), new String(data.toBytes(), StandardCharsets.UTF_8));
                context.commit(EventAction.CommitMessage);
            }
        });
        consumer.subscribe(event);
        consumer.start();
    }
}
