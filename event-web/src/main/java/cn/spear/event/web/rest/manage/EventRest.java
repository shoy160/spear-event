package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.*;
import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.business.service.EventService;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.model.EventRecord;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.web.model.cmd.EventCmd;
import cn.spear.event.web.model.cmd.EventEditCmd;
import cn.spear.event.web.model.cmd.OptionsCmd;
import cn.spear.event.web.model.vo.EventGroupQueryVO;
import cn.spear.event.web.model.vo.EventQueryVO;
import cn.spear.event.web.rest.BaseManageRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/7
 */
@RequiredArgsConstructor
@RestController("ManageEvent")
@RequestMapping("manage/event")
@Api(value = "事件管理", tags = "事件管理")
public class EventRest extends BaseManageRest {
    private final ConnectorAcl connectorAcl;

    private final EventService eventService;


    @GetMapping()
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
        return eventService.findByPaged(queryVO.getPage(), queryVO.getSize(), queryDTO);
    }

    @GetMapping("topics")
    @ApiOperation(value = "事件主题列表", notes = "事件主题列表")
    public List<String> topicList() {
        return eventService.findAllTopics();
    }

    @GetMapping("groups")
    @ApiOperation(value = "事件分组列表", notes = "事件分组列表")
    public List<EventGroupDTO> groups(@Valid EventGroupQueryVO queryVO) {
        EventQueryDTO queryDTO = toBean(queryVO, EventQueryDTO.class);
        return eventService.findGroups(queryDTO);
    }

    @GetMapping("{eventId}")
    @ApiOperation(value = "事件详情", notes = "事件详情")
    public EventDTO detail(@PathVariable Long eventId) {
        return eventService.detail(eventId);
    }

    @GetMapping("{eventId}/records")
    @ApiOperation(value = "事件消息", notes = "事件消息")
    public PagedDTO<EventRecord> getRecords(@PathVariable Long eventId, int size) {
        EventPO event = eventService.getById(eventId);
        return connectorAcl.getTopicRecords(size, event.getTopic(), null, null);
    }

    @PostMapping
    @ApiOperation(value = "创建事件", notes = "创建事件")
    public EventDTO create(@Valid @RequestBody EventCmd cmd) {
        EventCreateDTO dto = toBean(cmd, EventCreateDTO.class);
        return eventService.create(dto);
    }

    @PutMapping("{eventId}")
    @ApiOperation(value = "编辑事件", notes = "编辑事件")
    public EventDTO edit(@PathVariable Long eventId, @Valid @RequestBody EventEditCmd cmd) {
        boolean editResult = eventService.edit(eventId, cmd.getName(), cmd.getDesc(),
                cmd.getTags(), cmd.getSort());
        if (editResult) {
            return detail(eventId);
        }
        throw new BusinessException("更新事件失败");
    }

    @PutMapping("{eventId}/extend")
    @ApiOperation(value = "分区扩容", notes = "分区扩容")
    public boolean extend(@PathVariable Long eventId,
                          @ApiParam(required = true, value = "分区数") @RequestParam Integer partitions) {
        return eventService.extend(eventId, partitions);
    }

    @DeleteMapping("{eventId}")
    @ApiOperation(value = "删除事件", notes = "删除事件")
    public boolean remove(@PathVariable Long eventId) {
        return eventService.remove(eventId);
    }

    @PostMapping("export")
    @ApiOperation(value = "事件导入", notes = "事件导入")
    public boolean export(MultipartFile file) throws IOException {
        eventService.importEvent(file.getOriginalFilename(), file.getInputStream());
        return true;
    }

    @PutMapping("{eventId}/options")
    @ApiOperation(value = "事件配置", notes = "事件配置")
    public boolean options(@PathVariable Long eventId, @RequestBody OptionsCmd cmd
    ) {
        return eventService.setOptions(eventId, cmd.getOptions(), cmd.getAdd());
    }
}
