package cn.spear.event.web.model.cmd;

import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.JsonUtils;
import cn.hutool.core.date.DateUtil;
import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * event 事件实体
 *
 * @author luoyong
 * @date 2022/11/16
 */
@Getter
@Setter
public class EventCloudEvent {
    private String id;
    @ApiModelProperty("事件编码")
    @NotBlank(message = "事件编码不能为空")
    private String subject;
    @ApiModelProperty("事件类型")
    private String type;
    @ApiModelProperty("事件来源")
    private String source;
    @ApiModelProperty("事件内容类型，默认：json")
    private String dataContentType;
    private SpecVersion specVersion;
    @ApiModelProperty("事件发生的时间戳")
    private Long time;
    @ApiModelProperty("事件内容")
    @NotNull(message = "事件内容不能为空")
    private Serializable data;
    @ApiModelProperty("事件扩展")
    private Map<String, Serializable> extensions;

    public EventCloudEvent() {
        this.dataContentType = "json";
        this.time = DateUtil.current();
        this.extensions = new HashMap<>();
    }

    public static EventCloudEvent from(CloudEvent cloudEvent) {
        EventCloudEvent event = CommonUtils.toBean(cloudEvent, EventCloudEvent.class);
        if (null != cloudEvent.getData()) {
            event.setData(JsonUtils.json(cloudEvent.getData().toBytes(), Serializable.class));
        }
        event.setSpecVersion(cloudEvent.getSpecVersion());
        return event;
    }
}
