package cn.spear.event.web.rest.app;

import cn.spear.event.business.domain.dto.*;
import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.business.service.EventService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.web.model.cmd.EventCmd;
import cn.spear.event.web.model.cmd.EventEditCmd;
import cn.spear.event.web.model.vo.EventExplorerQueryVO;
import cn.spear.event.web.model.vo.EventGroupQueryVO;
import cn.spear.event.web.model.vo.EventQueryVO;
import cn.spear.event.web.rest.BaseAppRest;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 事件管理
 *
 * @author luoyong
 * @date 2023/1/5
 */
@Slf4j
@RequiredArgsConstructor
@RestController("EventApp")
@RequestMapping("app/event")
@Api(value = "事件管理", tags = "事件管理接口")
public class EventRest extends BaseAppRest {
    private final EventService eventService;
    private final ClientGrantService grantService;

    @GetMapping()
    @ApiOperation(value = "事件列表", notes = "事件列表")
    public List<EventDTO> list(@Valid EventGroupQueryVO queryVO) {
        EventQueryDTO queryDTO = toBean(queryVO, EventQueryDTO.class);
        queryDTO.setClientId(clientId());
        return eventService.findList(queryDTO);
    }

    @GetMapping("page")
    @ApiOperation(value = "事件分页", notes = "事件分页")
    public PagedDTO<EventPageDTO> page(
            @Valid EventQueryVO queryVO,
            @ApiParam(value = "是否刷新", defaultValue = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean refresh
    ) {
        if (Objects.equals(true, refresh)) {
            eventService.syncMessageCount();
        }
        EventQueryDTO queryDTO = toBean(queryVO, EventQueryDTO.class);
        queryDTO.setClientId(clientId());
        queryDTO.setPrivateId(privateId());
        return eventService.findByPaged(queryVO.getPage(), queryVO.getSize(), queryDTO);
    }

    @GetMapping("group")
    @ApiOperation(value = "事件分组列表", notes = "事件分组列表")
    public List<EventGroupDTO> groups(@Valid EventGroupQueryVO queryVO) {
        EventQueryDTO queryDTO = toBean(queryVO, EventQueryDTO.class);
        queryDTO.setClientId(clientId());
        if (StrUtil.isNotBlank(privateId())) {
            queryDTO.setPrivateId(privateId());
        }
        return eventService.findGroups(queryDTO);
    }

    @ApiIgnore
    @GetMapping("topics")
    @ApiOperation(value = "事件主题列表", notes = "事件主题列表")
    public List<String> topicList() {
        return eventService.findAllTopics();
    }

    @GetMapping("{eventId}")
    @ApiOperation(value = "事件详情", notes = "事件详情")
    public EventDTO detail(@PathVariable @ApiParam(value = "事件 ID", required = true) Long eventId) {
        return eventService.detail(eventId);
    }

    @GetMapping("detail")
    @ApiOperation(value = "事件详情", notes = "事件详情")
    public EventDTO detail(
            @RequestParam @ApiParam(value = "事件编码", required = true) String code) {
        return eventService.findByCode(code);
    }

    @PostMapping
    @ApiOperation(value = "创建事件", notes = "创建事件")
    public EventDTO create(@Valid @RequestBody EventCmd cmd) {
        EventCreateDTO dto = toBean(cmd, EventCreateDTO.class);
        if (EventTypeEnum.Private.equals(dto.getType()) && StrUtil.isNotBlank(privateId())) {
            dto.setPrivateId(privateId());
        }
        EventDTO event = eventService.create(dto);
        if (Objects.nonNull(event)) {
            grantService.grantByClient(clientId(), event, cmd.getConsumerGroup(), cmd.getProducerSource());
        }
        return event;
    }

    @PutMapping("{eventId}")
    @ApiOperation(value = "编辑事件", notes = "编辑事件")
    public EventDTO edit(
            @PathVariable @ApiParam(value = "事件 ID", required = true) Long eventId,
            @Valid @RequestBody EventEditCmd cmd
    ) {
        boolean editResult = eventService
                .editInApp(eventId, cmd.getName(), cmd.getDesc(), cmd.getTags(), cmd.getSort(), privateId());
        if (editResult) {
            return detail(eventId);
        }
        throw new BusinessException("编辑事件失败");
    }

    @PutMapping("")
    @ApiOperation(value = "编辑事件", notes = "编辑事件")
    public EventDTO edit(
            @RequestParam @ApiParam(value = "事件 ID", required = true) String code,
            @Valid @RequestBody EventEditCmd cmd
    ) {
        EventDTO event = eventService.findByCode(code);
        if (Objects.isNull(event)) {
            throw new BusinessException("事件编码不存在");
        }
        boolean editResult = eventService
                .editInApp(event.getId(), cmd.getName(), cmd.getDesc(), cmd.getTags(), cmd.getSort(), privateId());
        if (editResult) {
            return detail(event.getId());
        }
        throw new BusinessException("编辑事件失败");
    }

    @DeleteMapping("{eventId}")
    @ApiOperation(value = "删除事件", notes = "删除事件")
    public boolean remove(@PathVariable @ApiParam(value = "事件 ID", required = true) Long eventId) {
        return eventService.removeInApp(eventId, privateId());
    }

    @GetMapping("explorer")
    @ApiOperation(value = "explorer 事件", notes = "explorer 事件列表")
    public Object getAllPublicEvents(@Valid EventExplorerQueryVO query) {
        if (query.getTags() == null || query.getTags().length < 1) {
            return null;
        }
        log.info("tags:{}", query.getTags());
        EventGroupQueryVO queryVO = new EventGroupQueryVO();
        queryVO.setAppCode(query.getAppCode());
        log.info(queryVO.getAppCode());
        EventQueryDTO queryDTO = toBean(queryVO, EventQueryDTO.class);
        queryDTO.setClientId(clientId());
        HashMap<String, List<EventDTO>> data = new HashMap<>();
        for (String tag : query.getTags()) {
            data.put(tag, new ArrayList<>());
        }
        for (EventDTO eventDTO : eventService.findList(queryDTO)) {
            if (eventDTO.getTags() == null || eventDTO.getTags().isEmpty()) {
                continue;
            }
            //log.debug("tags:{}", eventDTO.getTags());
            for (String tag : eventDTO.getTags()) {
                if (data.get(tag) == null) {
                    continue;
                }
                data.get(tag).add(eventDTO);
            }
        }
        return data;
    }
}
