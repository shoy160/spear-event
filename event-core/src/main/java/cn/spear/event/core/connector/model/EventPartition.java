package cn.spear.event.core.connector.model;

import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/12/27
 */
@Getter
@Setter
public class EventPartition extends BaseDTO {
    private String topic;
    private int partition;

    public EventPartition() {
    }

    public EventPartition(String topic, int partition) {
        this.topic = topic;
        this.partition = partition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + partition;
        result = prime * result + Objects.hashCode(topic);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        EventPartition other = (EventPartition) obj;
        return partition == other.partition && Objects.equals(topic, other.topic);
    }

    @Override
    public String toString() {
        return String.format("%s-%d", this.topic, this.partition);
    }
}
