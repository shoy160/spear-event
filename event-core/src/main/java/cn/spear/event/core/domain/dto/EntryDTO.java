package cn.spear.event.core.domain.dto;

import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Key-Value
 *
 * @author shay
 * @date 2020/9/24
 */
@Getter
@Setter
@ToString
public class EntryDTO<TKey, TValue> implements Serializable {
    private static final long serialVersionUID = -2630196578792825551L;

    private TKey key;
    private TValue value;

    public EntryDTO() {
    }

    private EntryDTO(TKey key, TValue value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> EntryDTO<K, V> of(K key, V value) {
        return new EntryDTO<>(key, value);
    }

    public static <K, V> List<EntryDTO<K, V>> ofMap(Map<K, V> map) {
        if (MapUtil.isEmpty(map)) {
            return new ArrayList<>(0);
        }
        List<EntryDTO<K, V>> values = new ArrayList<>();
        for (Map.Entry<K, V> enumEntry : map.entrySet()) {
            values.add(EntryDTO.of(enumEntry.getKey(), enumEntry.getValue()));
        }
        return values;
    }
}
