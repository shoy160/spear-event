package cn.spear.event.core.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author shay
 * @date 2020/12/15
 */
@Getter
@Setter
@ToString
public class CacheItem<V> {

    private V value;
    /**
     * 最后访问时间
     */
    private long accessTime;
    /**
     * 过期时间
     */
    private long expireTime;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 命中次数
     */
    private int hitCount;

    public CacheItem(V value, long expireTime) {
        this.value = value;
        create(expireTime);
    }

    public V getValue() {
        update(0);
        return this.value;
    }

    public void create(long expireTime) {
        this.hitCount = 0;
        this.createTime = System.currentTimeMillis();
        if (expireTime > 0) {
            this.expireTime = expireTime;
        }
    }

    public void update(long expireTime) {
        this.hitCount++;
        this.accessTime = System.currentTimeMillis();
        if (expireTime > 0) {
            this.expireTime = expireTime;
        }
    }
}
