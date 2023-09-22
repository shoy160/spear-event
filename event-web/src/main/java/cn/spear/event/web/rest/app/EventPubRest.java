package cn.spear.event.web.rest.app;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.service.EventService;
import cn.spear.event.connector.kafka.delay.KafkaDelayManager;
import cn.spear.event.connector.redis.delay.RedisDelayManager;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.ConnectorManager;
import cn.spear.event.core.connector.Producer;
import cn.spear.event.core.domain.dto.ResultDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.web.config.BaseProperties;
import cn.spear.event.web.model.cmd.EventCloudEvent;
import cn.spear.event.web.rest.BaseAppRest;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * 事件中心
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Slf4j
@RequiredArgsConstructor
@RestController("EventPubApp")
@RequestMapping("app/event")
@Api(value = "事件发布", tags = "事件发布接口")
public class EventPubRest extends BaseAppRest {
    private final BaseProperties config;
    private final ConnectorManager connectorManager;
    private final KafkaDelayManager kafkaDelayManager;
    private final RedisDelayManager redisDelayManager;

    private final ClientGrantService grantService;
    private final EventService eventService;

    @PutMapping("pub")
    @ApiOperation(value = "发布事件", notes = "发布事件，返回事件 ID。参考：https://steamory.feishu.cn/docx/JXArdvWrAoUx33xISebcr8YRnIh")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "event", value = "事件实体", dataTypeClass = Object.class, paramType = "body"),
            @ApiImplicitParam(name = "ce-id", value = "事件 ID", paramType = "header", example = "xsdfe88dfdudf8", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-specversion", value = "版本号，固定值：1.0", paramType = "header", example = "1.0", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-subject", value = "事件编码", paramType = "header", example = "authing.user.created", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-type", value = "事件类型，用来过滤和转发，类似 TAG", paramType = "header", example = "Java", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-source", value = "事件来源", paramType = "header", example = "/authing/console/user/created", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-time", value = "事件产生的时间", paramType = "header", example = "2022-12-23T02:57:52.36Z", dataTypeClass = OffsetDateTime.class),
            @ApiImplicitParam(name = "ce-delayminutes", value = "扩展：事件延迟时间（分钟）", paramType = "header", example = "5", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "ce-delayhours", value = "扩展：事件延迟时间（小时）", paramType = "header", example = "2", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "ce-delaydays", value = "扩展：事件延迟时间（天）", paramType = "header", example = "1", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "ce-delayat", value = "扩展：事件延迟时间点（时间戳）", paramType = "header", example = "1671764621902", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "ce-privateid", value = "扩展：私有 ID,发布私有事件时必传", paramType = "header", example = "1671764621902", dataTypeClass = String.class),
            @ApiImplicitParam(name = "ce-extension", value = "扩展：用户池 ID", paramType = "header", example = "1671764621902", dataTypeClass = String.class)
    })
    public ResultDTO<String> pub(@RequestBody CloudEvent event) {
        String code = event.getSubject();
        String publishId = buildAndPublish(event);
        if (StrUtil.isNotBlank(publishId)) {
            //更新消息数
            eventService.syncMessageCount(code);
        }
        return success(publishId);
    }

    private String buildAndPublish(CloudEvent cloudEvent) {
        Object privateId = cloudEvent.getExtension(Constants.ATTR_PRIVATE_ID);
        GrantDTO grant = null;
        if (config.isEnableClient()) {
            grant = grantService
                    .findGrant(clientId(), GrantTypeEnum.Producer, cloudEvent.getSubject(), privateId);
        }
        CloudEventBuilder builder = CloudEventBuilder.from(cloudEvent);
        if (StrUtil.isBlank(cloudEvent.getId())) {
            builder.withId(IdUtil.fastSimpleUUID());
        }
        if (Objects.nonNull(grant)) {
            if (StrUtil.isBlank(cloudEvent.getType())) {
                builder.withType(grant.getGroup());
            }
            if (null == cloudEvent.getSource()) {
                builder.withSource(URI.create(grant.getSource()));
            }
        }
        if (null == cloudEvent.getTime()) {
            builder.withTime(OffsetDateTime.now());
        }
        CloudEvent event = builder.build();
        String delayResult = checkDelay(event);
        if (StrUtil.isNotBlank(delayResult)) {
            return delayResult;
        }
        return publish(event);
    }

    private String checkDelay(CloudEvent cloudEvent) {
        // 按分钟延迟
        int delayMinutes = Convert.toInt(cloudEvent.getExtension(Constants.ATTR_DELAY_MINUTES), 0);
        if (0 >= delayMinutes) {
            Object delayValue = cloudEvent.getExtension(Constants.ATTR_DELAY_HOURS);
            if (Objects.nonNull(delayValue)) {
                delayMinutes = Convert.toInt(delayValue, 0) * 60;
            }
        }
        if (0 >= delayMinutes) {
            Object delayValue = cloudEvent.getExtension(Constants.ATTR_DELAY_DAYS);
            if (Objects.nonNull(delayValue)) {
                delayMinutes = Convert.toInt(delayValue, 0) * 24 * 60;
            }
        }
        if (delayMinutes > 0) {
            if (kafkaDelayManager.isStarted()) {
                kafkaDelayManager.sendDelay(cloudEvent, delayMinutes);
                return cloudEvent.getId();
            }
            if (redisDelayManager.isStarted()) {
                redisDelayManager.sendDelay(cloudEvent, delayMinutes);
                return cloudEvent.getId();
            }
        }
        // 按时间点延迟
        long sendAt = Convert.toLong(cloudEvent.getExtension(Constants.ATTR_DELAY_SEND_TIME), 0L);
        if (sendAt > System.currentTimeMillis() && redisDelayManager.isStarted()) {
            redisDelayManager.sendDelay(cloudEvent, new Date(sendAt));
            return cloudEvent.getId();
        }
        return null;
    }

    private String publish(CloudEvent cloudEvent) {
        try {
            Producer producer = connectorManager
                    .createProducer(String.valueOf(clientId()), clientSecret());
            producer.publish(cloudEvent);
        } catch (Exception e) {
            EventCloudEvent eventCloudEvent = EventCloudEvent.from(cloudEvent);
            log.error(String.format("发布消息异常：%s", JsonUtils.toJson(eventCloudEvent)), e);
            throw new BusinessException("发布消息异常");
        }
        return cloudEvent.getId();
    }
}
