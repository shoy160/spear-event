package cn.spear.event.core.domain.dto;

/**
 * @author luoyong
 * @date 2022/12/22
 */
public class StringEntry extends EntryDTO<String, String> {

    public static StringEntry of(String key, String value) {
        StringEntry entry = new StringEntry();
        entry.setKey(key);
        entry.setValue(value);
        return entry;
    }
}
