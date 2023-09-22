package cn.spear.event.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.business.dao.EventMapper;
import cn.spear.event.business.dao.EventSchemaMapper;
import cn.spear.event.business.domain.dto.*;
import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.enums.OptionsEnum;
import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.business.domain.po.EventSchemaPO;
import cn.spear.event.business.domain.po.GroupPO;
import cn.spear.event.business.service.AppService;
import cn.spear.event.business.service.EventService;
import cn.spear.event.business.service.GroupService;
import cn.spear.event.business.utils.ExcelUtils;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.Constants;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.JsonUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 事件服务
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, EventPO> implements EventService {

    @Value("${spear.event.schema-template:}")
    private String schemaTemplate;
    private final Snowflake snowflake;
    private final ConnectorAcl connectorAcl;
    private final Cache<String, EventDTO> eventCache;

    private final EventMapper eventMapper;
    private final EventSchemaMapper eventSchemaMapper;

    private final AppService appService;
    private final GroupService groupService;

    @Override
    public PagedDTO<EventPageDTO> findByPaged(int page, int size, EventQueryDTO queryDTO) {
        LambdaQueryChainWrapper<EventPO> wrapper = buildQueryWrapper(queryDTO);
        Page<EventPO> eventPaged =
                wrapper.orderByDesc(EventPO::getCreatedAt)
                        .page(new Page<>(page, size));
        List<String> appCodes = eventPaged.getRecords()
                .stream().map(EventPO::getAppCode)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AppShowDTO> appShowMap = appService.findShowInfoByCode(appCodes);
        return PagedUtils.convert(eventPaged, v -> convertPage(v, appShowMap));
    }

    @Override
    public List<EventDTO> findList(EventQueryDTO queryDTO) {
        LambdaQueryChainWrapper<EventPO> wrapper = buildQueryWrapper(queryDTO);
        return wrapper
                .orderByDesc(EventPO::getCreatedAt)
                .list()
                .stream()
                .map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<EventGroupDTO> findGroups(EventQueryDTO queryDTO) {
        List<EventGroupDTO> groups = new ArrayList<>(0);
        List<EventDTO> eventList = findList(queryDTO);
        if (CollUtil.isEmpty(eventList)) {
            return groups;
        }
        final String otherGroup = "其他";
        Map<String, List<EventDTO>> groupMap = new HashMap<>();
        for (EventDTO eventDTO : eventList) {
            List<EventDTO> currentList = new ArrayList<>();
            currentList.add(eventDTO);
            if (CollUtil.isEmpty(eventDTO.getTags())) {
                groupMap.merge(otherGroup, currentList, (o, n) -> {
                    o.addAll(n);
                    return o;
                });
            } else {
                for (String tag : eventDTO.getTags()) {
                    groupMap.merge(tag, currentList, (o, n) -> {
                        o.addAll(n);
                        return o;
                    });
                }
            }
        }
        // 排序
        Map<String, Integer> tagSorts = new HashMap<>();
        if (StrUtil.isNotBlank(queryDTO.getAppCode())) {
            List<GroupDTO> tagList = groupService.findByAppCode(queryDTO.getAppCode(), GroupTypeEnum.Tag);
            if (CollUtil.isNotEmpty(tagList)) {
                tagSorts = tagList.stream()
                        .collect(Collectors.toMap(GroupDTO::getName, GroupDTO::getSort));
            }
        }
        // 其他排最后
        tagSorts.put(otherGroup, -1);
        final Map<String, Integer> finalSorts = tagSorts;
        return groupMap.entrySet().stream()
                .map(t -> {
                    String groupName = t.getKey();
                    List<EventDTO> events = t.getValue();
                    events.sort((a, b) -> b.getSort().compareTo(a.getSort()));
                    return new EventGroupDTO(groupName, events);
                })
                .sorted((a, b) -> {
                    Integer sortA = MapUtil.getInt(finalSorts, a.getGroupName(), 0);
                    Integer sortB = MapUtil.getInt(finalSorts, b.getGroupName(), 0);
                    if (Objects.equals(sortA, sortB)) {
                        return a.getGroupName().compareTo(b.getGroupName());
                    } else {
                        return sortB.compareTo(sortA);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllTopics() {
        return lambdaQuery().select(EventPO::getCode)
                .list().stream()
                .map(EventPO::getCode)
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }

    @Override
    public EventDTO detail(Long eventId) {
        EventPO model = getById(eventId);
        if (Objects.isNull(model)) {
            throw new BusinessException("事件 ID 不存在");
        }
        return convert(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventDTO create(EventCreateDTO dto) {
        EventPO model = CommonUtils.toBean(dto, EventPO.class);
        model.setId(snowflake.nextId());
        model.setOptions(OptionsEnum.Arrangeable.getValue());
        checkEventCode(model);
        boolean exists = lambdaQuery().eq(EventPO::getCode, model.getCode()).exists();
        if (exists) {
            throw new BusinessException("事件编码已存在");
        }
        List<String> tags = dto.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            checkTags(tags, dto.getAppCode());
            model.setTags(JsonUtils.toJson(tags));
        }
        int replications = connectorAcl.createTopic(model.getTopic(), dto.getPartition(), null, dto.getRetention());
        model.setReplications(replications);
        baseMapper.cleanByCode(dto.getCode());
        boolean saveResult = save(model);
        if (saveResult) {
            String cacheKey = eventCacheKey(model.getCode());
            eventCache.put(cacheKey, CommonUtils.toBean(model, EventDTO.class), 7, TimeUnit.HOURS);
        }
        return convert(model);
    }

    private void checkTags(List<String> tags, String appCode) {
        if (StrUtil.isBlank(appCode)) {
            return;
        }
        List<String> appTags = groupService.findByAppCode(appCode, GroupTypeEnum.Tag)
                .stream().map(GroupDTO::getName)
                .collect(Collectors.toList());
        List<GroupPO> newTags = tags.stream()
                .filter(i -> !appTags.contains(i))
                .map(i -> GroupPO.builder()
                        .id(snowflake.nextId())
                        .appCode(appCode)
                        .name(i)
                        .type(GroupTypeEnum.Tag)
                        .build())
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(newTags)) {
            groupService.saveBatch(newTags);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(Long eventId, String name, String desc, List<String> tags, Integer sort) {
        EventPO event = getById(eventId);
        if (null == event) {
            throw new BusinessException("事件不存在");
        }
        boolean updateResult = lambdaUpdate().eq(EventPO::getId, eventId)
                .set(StrUtil.isNotBlank(name), EventPO::getName, name)
                .set(EventPO::getDesc, desc)
                .set(EventPO::getTags, Objects.isNull(tags) ? null : JsonUtils.toJson(tags))
                .set(Objects.nonNull(sort), EventPO::getSort, sort)
                .update(new EventPO());
        checkTags(tags, event.getAppCode());
        if (updateResult) {
            eventCache.remove(eventCacheKey(event.getCode()));
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editInApp(
            Long eventId, String name, String desc, List<String> tags, Integer sort, String privateId
    ) {
        EventPO event = getById(eventId);
        if (null == event) {
            throw new BusinessException("事件不存在");
        }
        if (EventTypeEnum.Private.equals(event.getType())
                && !Objects.equals(event.getPrivateId(), privateId)) {
            throw new BusinessException("无法修改其他用户池私有事件");
        }
        boolean updateResult = lambdaUpdate().eq(EventPO::getId, eventId)
                .set(StrUtil.isNotBlank(name), EventPO::getName, name)
                .set(EventPO::getDesc, desc)
                .set(EventPO::getTags, Objects.isNull(tags) ? null : JsonUtils.toJson(tags))
                .set(Objects.nonNull(sort), EventPO::getSort, sort)
                .update(new EventPO());
        checkTags(tags, event.getAppCode());
        if (updateResult) {
            eventCache.remove(eventCacheKey(event.getCode()));
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean extend(Long eventId, Integer partitions) {
        EventPO event = getById(eventId);
        if (null == event) {
            throw new BusinessException("事件不存在");
        }
        if (event.getPartition() >= partitions) {
            throw new BusinessException(String.format("分区数应大于当前分区数: %d", event.getPartition()));
        }
        boolean updateResult = lambdaUpdate()
                .eq(EventPO::getId, eventId)
                .set(EventPO::getPartition, partitions)
                .update(new EventPO());
        if (updateResult) {
            connectorAcl.extendPartitions(event.getTopic(), partitions);
            eventCache.remove(eventCacheKey(event.getCode()));
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long eventId) {
        EventPO model = getById(eventId);
        if (null == model) {
            throw new BusinessException("事件 ID 不存在");
        }
        boolean removeResult = removeById(eventId);
        if (removeResult) {
            connectorAcl.deleteTopic(model.getTopic());
            eventCache.remove(eventCacheKey(model.getCode()));
        }
        return removeResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeInApp(Long eventId, String privateId) {
        EventPO model = getById(eventId);
        if (null == model) {
            throw new BusinessException("事件 ID 不存在");
        }
        if (Objects.equals(model.getType(), EventTypeEnum.Public)) {
            throw new BusinessException("公有事件无法删除");
        }
//        if (Objects.equals(model.getType(), EventTypeEnum.Private)
//                && !Objects.equals(model.getPrivateId(), privateId)) {
//            throw new BusinessException("无法删除其他私有 ID 事件");
//        }
        boolean removeResult = removeById(eventId);
        if (removeResult) {
            connectorAcl.deleteTopic(model.getTopic());
            eventCache.remove(eventCacheKey(model.getCode()));
        }
        return removeResult;
    }

    @Override
    public EventDTO findByCode(String eventCode) {
        String cacheKey = eventCacheKey(eventCode);
        EventDTO event = eventCache.getOrPut(cacheKey, key -> {
            EventPO model = baseMapper.queryByCode(eventCode);
            if (null == model) {
                return new EventDTO();
            }
            return convert(model);
        }, 7, TimeUnit.HOURS);
        return null != event && null == event.getId() ? null : event;
    }

    @Override
    public void syncMessageCount() {
        final int size = 100;
        int current = 0;
        while (true) {
            boolean hasNext = syncMessageCountProcess(null, current, size);
            if (!hasNext) {
                break;
            }
            current += size;
        }
    }

    @Override
    public void syncMessageCount(String eventCode) {
        syncMessageCountProcess(eventCode, 0, 1);
    }

    @Override
    public void importEvent(String originalFilename, InputStream inputStream) throws IOException {
        ExcelUtils.checkFile(originalFilename);
        List<EventCreateDTO> events = ExcelUtils.importEvent(inputStream);
        if (CollUtil.isEmpty(events)) {
            return;
        }
        for (EventCreateDTO event : events) {
            try {
                create(event);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public EventSchemaDTO getSchema(Long id) {
        EventSchemaDTO schema = eventSchemaMapper.getSchemaById(id);
        EventPO event = eventMapper.queryById(id);
        return getSchema(event, schema);
    }

    @Override
    public EventSchemaDTO getSchema(String code) {
        EventSchemaDTO schema = eventSchemaMapper.getSchemaByCode(code);
        EventPO event = eventMapper.queryByCode(code);
        return getSchema(event, schema);
    }

    private EventSchemaDTO getSchema(EventPO event, EventSchemaDTO schema) {
        if (Objects.isNull(event)) {
            throw new BusinessException("查询事件不存在");
        }
        if (Objects.isNull(schema)) {
            schema = new EventSchemaDTO(event.getId(), event.getCode(), Constants.STR_EMPTY);
        }
        if (StrUtil.isNotBlank(schemaTemplate) && StrUtil.isBlank(schema.getSchema())) {
            Map<String, Object> sourceMap = BeanUtil.beanToMap(event);
            String template = StrUtil.format(schemaTemplate, sourceMap);
            schema.setSchema(template);
        }
        return schema;
    }

    @Override
    public boolean saveSchema(Long id, String schema) {
        EventDTO event = detail(id);
        return saveSchema(event, schema, true, null);
    }

    @Override
    public boolean saveSchema(Long id, String schema, String privateId) {
        EventDTO event = detail(id);
        return saveSchema(event, schema, false, privateId);
    }

    @Override
    public boolean saveSchema(String code, String schema) {
        EventDTO event = findByCode(code);
        return saveSchema(event, schema, true, null);
    }

    @Override
    public boolean saveSchema(String code, String schema, String privateId) {
        EventDTO event = findByCode(code);
        return saveSchema(event, schema, false, privateId);
    }

    @Override
    public boolean setOptions(Long id, int options, boolean isAnd) {
        EventPO model = lambdaQuery().eq(EventPO::getId, id)
                .select(EventPO::getId, EventPO::getOptions)
                .one();
        if (Objects.isNull(model)) {
            throw new BusinessException(String.format("事件[%d]不存在", id));
        }
        options = options | model.getOptions();
        options = isAnd ? options : (options ^ model.getOptions());
        options = Math.max(0, options);
        return lambdaUpdate()
                .eq(EventPO::getId, id)
                .set(EventPO::getOptions, options)
                .update();
    }

    private boolean saveSchema(EventDTO event, String schema, boolean isManage, String privateId) {
        if (Objects.isNull(event)) {
            throw new BusinessException("事件不存在");
        }
        // 私有事件
        if (!isManage && EventTypeEnum.Private.equals(event.getType())) {
            if (!Objects.equals(event.getPrivateId(), privateId)) {
                throw new BusinessException("无法修改其他私有 ID 下的事件");
            }
        }
        CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().ignoreError();
        EventSchemaPO target = BeanUtil.toBean(event, EventSchemaPO.class, copyOptions);
        target.setSchema(schema);
        return eventSchemaMapper.saveOrUpdate(target);
    }

    /**
     * 事件编码检测
     *
     * @param model 事件实体
     */
    private void checkEventCode(EventPO model) {
        if (StrUtil.isBlank(model.getCode())) {
            throw new BusinessException("事件编码不能为空");
        }
        String appCode = model.getAppCode();
        if (EventTypeEnum.Public.equals(model.getType())) {
            model.setPrivateId(null);
        } else {
            // 自定义事件
            String privateId = model.getPrivateId();
            if (StrUtil.isBlank(privateId)) {
                throw new BusinessException("私有事件必须有私有 ID");
            }
            AppDTO customApp;
            if (StrUtil.isBlank(appCode) && model.getCode().startsWith("spear.")) {
                appCode = "spear";
            }
            if (StrUtil.isNotBlank(appCode)) {
                customApp = appService.findByCode(appCode);
                if (Objects.isNull(customApp)) {
                    throw new BusinessException("自定义应用编码不存在");
                }
            } else {
                customApp = appService.findOrCreateCustomApp(privateId);
            }
            appCode = customApp.getCode();
        }
        if (StrUtil.isNotBlank(appCode)) {
            if (!model.getCode().startsWith(appCode.concat(Constants.STR_LEVEL))) {
                model.setCode(appCode.concat(Constants.STR_LEVEL).concat(model.getCode()));
            }
        }
        String[] codes = model.getCode().split("\\.");
        if (codes.length > 1) {
            model.setAppCode(codes[0]);
        }
        if (codes.length > 2) {
            model.setModuleCode(codes[1]);
        }
    }

    private LambdaQueryChainWrapper<EventPO> buildQueryWrapper(EventQueryDTO queryDTO) {
        String parent = queryDTO.getParent();
        if (StrUtil.isNotBlank(parent) && !parent.endsWith(Constants.STR_LEVEL)) {
            parent = parent.concat(Constants.STR_LEVEL);
        }
        LambdaQueryChainWrapper<EventPO> wrapper = lambdaQuery()
                .eq(Objects.nonNull(queryDTO.getType()), EventPO::getType, queryDTO.getType())
                .eq(StrUtil.isNotBlank(queryDTO.getAppCode()), EventPO::getAppCode, queryDTO.getAppCode())
                .likeRight(StrUtil.isNotBlank(parent), EventPO::getCode, parent)
                .like(StrUtil.isNotBlank(queryDTO.getCode()), EventPO::getCode, queryDTO.getCode())
                .like(StrUtil.isNotBlank(queryDTO.getName()), EventPO::getName, queryDTO.getName())
                .apply(StrUtil.isNotBlank(queryDTO.getTag()), "jsonb_contains(tags::jsonb,{0}::jsonb)", JsonUtils.toJson(queryDTO.getTag()))
                .apply(Objects.nonNull(queryDTO.getOptions()), "(options & {0})={0}", queryDTO.getOptions());
        if (Objects.nonNull(queryDTO.getClientId())) {
            // 客户端查询
            if (StrUtil.isNotBlank(queryDTO.getPrivateId())) {
                wrapper.and(t -> t.eq(EventPO::getPrivateId, queryDTO.getPrivateId())
                        .or()
                        .eq(EventPO::getType, EventTypeEnum.Public)
                );
            } else {
                wrapper.eq(EventPO::getType, EventTypeEnum.Public);
            }
        } else {
            // 管理平台查询
            wrapper.eq(StrUtil.isNotBlank(queryDTO.getPrivateId()), EventPO::getPrivateId, queryDTO.getPrivateId());
        }
        // 排序值排序
        if (Objects.nonNull(queryDTO.getSortWay())) {
            switch (queryDTO.getSortWay()) {
                case ascend:
                    wrapper.orderByAsc(EventPO::getSort);
                    break;
                case descend:
                    wrapper.orderByDesc(EventPO::getSort);
                    break;
                default:
            }
        }
        return wrapper;
    }

    private boolean syncMessageCountProcess(String code, int current, int size) {
        LambdaQueryChainWrapper<EventPO> wrapper = lambdaQuery();
        if (StrUtil.isNotBlank(code)) {
            wrapper.eq(EventPO::getCode, code);
        }
        Map<String, Long> countMap = wrapper
                .select(EventPO::getCode, EventPO::getMessageCount)
                .orderByDesc(EventPO::getCreatedAt)
                .last(String.format("LIMIT %d OFFSET %d", size, current))
                .list()
                .stream()
                .collect(Collectors.toMap(EventPO::getCode, EventPO::getMessageCount));
        Map<String, Long> messageCounts = connectorAcl.getMessageCounts(countMap.keySet());
        for (Map.Entry<String, Long> entry : messageCounts.entrySet()) {
            Long count = countMap.get(entry.getKey());
            if (Objects.equals(count, entry.getValue())) {
                continue;
            }
            lambdaUpdate()
                    .eq(EventPO::getCode, entry.getKey())
                    .set(EventPO::getMessageCount, entry.getValue())
                    .update();
        }
        return Objects.equals(size, countMap.size());
    }

    private EventPageDTO convertPage(EventPO eventModel, Map<String, AppShowDTO> appShowMap) {
        CopyOptions copyOptions = CopyOptions.create().ignoreError();
        EventPageDTO eventPageDTO = BeanUtil.toBean(eventModel, EventPageDTO.class, copyOptions);
        if (StrUtil.isNotBlank(eventModel.getTags())) {
            eventPageDTO.setTags(JsonUtils.jsonList(eventModel.getTags(), String.class));
        }
        String appCode = eventModel.getAppCode();
        if (CollUtil.isNotEmpty(appShowMap)) {
            eventPageDTO.setApp(appShowMap.getOrDefault(appCode, new AppShowDTO(appCode)));
        } else {
            eventPageDTO.setApp(new AppShowDTO(appCode));
        }
        return eventPageDTO;
    }

    private EventDTO convert(EventPO eventModel) {
        EventDTO eventDTO = CommonUtils.toBean(eventModel, EventDTO.class);
        if (StrUtil.isNotBlank(eventModel.getTags())) {
            eventDTO.setTags(Optional
                    .ofNullable(JsonUtils.jsonList(eventModel.getTags(), String.class))
                    .orElse(new ArrayList<>())
            );
        }
        return eventDTO;
    }

    private static String eventCacheKey(String code) {
        return String.format("event:%s", code);
    }
}
