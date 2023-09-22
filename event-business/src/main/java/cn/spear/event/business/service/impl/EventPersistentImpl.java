package cn.spear.event.business.service.impl;

import cn.spear.event.business.domain.po.EventDataPO;
import cn.spear.event.business.service.EventDataService;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.EventPersistent;
import cn.spear.event.core.exception.BusinessException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/12/9
 */
@Service
@RequiredArgsConstructor
public class EventPersistentImpl implements EventPersistent {
    private final EventDataService dataService;

    @Override
    public boolean save(String id, CloudEvent cloudEvent, Date sendAt) {
        if (null != dataService.getById(id)) {
            return true;
        }
        EventDataPO model = createModel(id, cloudEvent);
        if (null != sendAt) {
            model.setSendAt(sendAt);
        }
        return dataService.save(model);
    }

    @Override
    public CloudEvent get(String id) {
        EventDataPO model = dataService.getById(id);
        if (Objects.isNull(model)) {
            return null;
        }
        EventFormat eventFormat = createFormat();
        CloudEvent cloudEvent = eventFormat.deserialize(model.getData());
        if (StrUtil.isNotBlank(model.getTopic())
                && !Objects.equals(model.getTopic(), cloudEvent.getSubject())) {
            cloudEvent = new CloudEventBuilder(cloudEvent)
                    .withSubject(model.getTopic())
                    .build();
        }
        return cloudEvent;
    }

    @Override
    public boolean batchSave(Map<String, CloudEvent> cloudEvents) {
        if (MapUtil.isEmpty(cloudEvents)) {
            return false;
        }
        if (1 == cloudEvents.size()) {
            String id = cloudEvents.keySet().iterator().next();
            return save(id, cloudEvents.get(id));
        }
        List<String> existsIds = dataService
                .lambdaQuery()
                .in(EventDataPO::getId, cloudEvents.keySet().toArray())
                .select(EventDataPO::getId)
                .list()
                .stream()
                .map(EventDataPO::getId)
                .collect(Collectors.toList());
        List<EventDataPO> modelList = new ArrayList<>(cloudEvents.size());
        for (Map.Entry<String, CloudEvent> entry : cloudEvents.entrySet()) {
            if (existsIds.contains(entry.getKey())) {
                continue;
            }
            modelList.add(createModel(entry.getKey(), entry.getValue()));
        }
        return dataService.saveBatch(modelList, IService.DEFAULT_BATCH_SIZE);
    }

    @Override
    public Map<String, CloudEvent> batchGet(Collection<String> ids) {
        List<EventDataPO> eventList = dataService.getBaseMapper()
                .selectBatchIds(ids);
        if (CollUtil.isEmpty(eventList)) {
            return new HashMap<>(0);
        }
        return eventList.stream().collect(Collectors.toMap(EventDataPO::getId, v -> {
            EventFormat eventFormat = createFormat();
            return eventFormat.deserialize(v.getData());
        }));
    }

    @Override
    public boolean remove(String id) {
        return dataService.deleteById(id);
    }

    @Override
    public boolean batchRemove(Collection<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        return dataService.batchDelete(ids) > 0;
    }

    private EventDataPO createModel(String id, CloudEvent cloudEvent) {
        EventDataPO model = new EventDataPO();
        model.setId(id);
        long sendAt = Convert.toLong(cloudEvent.getExtension(Constants.ATTR_DELAY_SEND_TIME), 0L);
        if (sendAt > 0) {
            model.setSendAt(new Date(sendAt));
            cloudEvent = new CloudEventBuilder(cloudEvent)
                    .withoutExtension(Constants.ATTR_DELAY_SEND_TIME)
                    .build();
        }
        EventFormat eventFormat = createFormat();
        model.setTopic(cloudEvent.getSubject());
        byte[] eventData = eventFormat.serialize(cloudEvent);
        model.setData(eventData);
        return model;
    }

    private EventFormat createFormat() {
        EventFormat eventFormat = EventFormatProvider
                .getInstance()
                .resolveFormat(JsonFormat.CONTENT_TYPE);
        if (Objects.isNull(eventFormat)) {
            throw new BusinessException("CloudEvent 序列化异常");
        }
        return eventFormat;
    }
}
