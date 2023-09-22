package cn.spear.event.core.connector.model;

import cn.spear.event.core.domain.dto.StringEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author luoyong
 * @date 2022/12/22
 */
@Getter
@Setter
public class EventRecord {
    private String topic;

    private Integer partition;

    private Long offset;

    private Long timestamp;

    private List<StringEntry> headers;

    private String key;

    private String value;
}
