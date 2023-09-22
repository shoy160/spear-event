package cn.spear.event.core.cache;

import cn.spear.event.core.AppContext;
import cn.spear.event.core.Constants;
import cn.spear.event.core.lang.Action;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.core.utils.TypeUtils;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import java.util.concurrent.TimeUnit;

/**
 * 基础缓存类
 *
 * @author shay
 * @date 2021/03/06
 */
public abstract class BaseCache<K, V> implements Cache<K, V> {
    private final String region;

    protected BaseCache() {
        this(Constants.STR_EMPTY);
    }

    protected BaseCache(String region) {
        this.region = region;
    }

    @Override
    public long defaultExpired() {
        //随机1-10分钟
        int expired = RandomUtil.randomInt(60, 600);
        return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expired);
    }

    @Override
    public String getRegion() {
        if (StrUtil.isBlank(this.region)) {
            //默认区域去 AppName
            String appName = AppContext.getAppName();
            if (StrUtil.isNotBlank(appName)) {
                return appName;
            }
        }
        return this.region;
    }

    @Override
    public void info() {
    }

    @Override
    public void keyExpired(Action<K> expiredAction, Class<K> clazz) {
    }

    protected String stringKey(K key) {
        return stringKey(key, true);
    }

    protected String stringKey(K key, boolean includeRegion) {
        if (null == key) {
            return Constants.STR_EMPTY;
        }
        String stringKey;
        if (TypeUtils.isSimple(key)) {
            stringKey = key.toString();
        } else {
            stringKey = JsonUtils.toJson(key);
        }
        if (includeRegion && StrUtil.isNotBlank(this.getRegion())) {
            return this.getRegion()
                    .concat(Constants.STR_REGION)
                    .concat(stringKey);
        }
        return stringKey;
    }
}
