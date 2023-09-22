package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.EventDTO;
import cn.spear.event.business.service.EventService;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.enums.ConsumerState;
import cn.spear.event.core.connector.model.EventConsumer;
import cn.spear.event.core.connector.model.EventTopicMemberDetail;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.vo.PageVO;
import cn.spear.event.web.rest.BaseManageRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 订阅组相关接口
 *
 * @author luoyong
 * @date 2022/12/28
 */
@RequiredArgsConstructor
@RestController("ManageConsumer")
@RequestMapping("manage/consumer")
@Api(value = "订阅组管理", tags = "订阅组管理")
public class ConsumerRest extends BaseManageRest {
    private final ConnectorAcl connectorAcl;
    private final EventService eventService;

    @GetMapping("paged")
    @ApiOperation(value = "订阅组分页", notes = "订阅组分页")
    public PagedDTO<EventConsumer> paged(
            @Valid PageVO pageVO,
            @ApiParam(value = "订阅组") @RequestParam(required = false) String groupId,
            @ApiParam(value = "主题") @RequestParam(required = false) String topic,
            @ApiParam(value = "事件 ID") @RequestParam(required = false) Long eventId,
            @ApiParam(value = "状态") @RequestParam(required = false) ConsumerState state
    ) {
        if (Objects.nonNull(eventId)) {
            EventDTO event = eventService.detail(eventId);
            topic = event.getTopic();
        }
        return connectorAcl.getConsumers(pageVO.getPage(), pageVO.getSize(), groupId, topic, state, null);
    }

    @GetMapping("member")
    @ApiOperation(value = "订阅组详情", notes = "订阅组详情")
    public List<EventTopicMemberDetail> getMemberDetails(
            @ApiParam(value = "订阅组", required = true) @RequestParam() String groupId,
            @ApiParam(value = "主题") @RequestParam(required = false) String topic,
            @ApiParam(value = "事件 ID") @RequestParam(required = false) Long eventId
    ) {
        if (Objects.nonNull(eventId)) {
            EventDTO event = eventService.detail(eventId);
            topic = event.getTopic();
        }
        return connectorAcl.getConsumerDetails(groupId, topic);
    }
}
