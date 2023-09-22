package cn.spear.event.core.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author shoy
 * @date 2022/9/30
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncCacheKey {
    private String region;
    private String key;
    private String factoryId;
}
