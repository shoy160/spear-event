package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.ConfigDTO;
import cn.spear.event.business.domain.po.ConfigPO;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.core.utils.TypeUtils;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author leizeyu
 * @date 2023/3/6
 */
public interface ConfigService extends IService<ConfigPO> {

    /**
     * 配置分页查询
     *
     * @param page    page
     * @param size    size
     * @param key     key
     * @param prefix  前缀
     * @param profile 环境
     * @return paged of config
     */
    PagedDTO<ConfigDTO> page(int page, int size, String key, String prefix, String profile);

    /**
     * 查询所有缓存 Key
     *
     * @param prefix  前缀
     * @param profile 环境
     * @return list of key
     */
    List<String> allKeys(String prefix, String profile);

    /**
     * 查询所有缓存 Key
     *
     * @param profile 环境
     * @return list of key
     */
    default List<String> allKeys(String profile) {
        return allKeys(null, profile);
    }

    /**
     * 查询所有缓存 Keys
     *
     * @return list of key
     */
    default List<String> allKeys() {
        return allKeys(null, null);
    }

    /**
     * 配置修改
     *
     * @param key     key
     * @param value   value
     * @param profile 环境
     * @return boolean
     */
    boolean save(String key, Object value, String profile);

    /**
     * 保存配置
     *
     * @param key   key
     * @param value value
     * @return boolean
     */
    default boolean save(String key, Object value) {
        return save(key, value, null);
    }

    /**
     * 获取配置
     *
     * @param key     key
     * @param profile profile
     * @return value
     */
    String get(String key, String profile);

    /**
     * 获取配置
     *
     * @param key key
     * @return value
     */
    default String get(String key) {
        return get(key, null, String.class);
    }

    /**
     * 获取配置
     *
     * @param key     key
     * @param profile profile
     * @return value
     */
    Object getObject(String key, String profile);

    /**
     * 获取配置
     *
     * @param key key
     * @return value
     */
    default Object getObject(String key) {
        return getObject(key, null);
    }

    /**
     * 删除配置
     *
     * @param key     配置 key
     * @param profile profile
     * @return boolean
     */
    boolean remove(String key, String profile);

    /**
     * 删除配置
     *
     * @param key 配置 key
     * @return boolean
     */
    default boolean remove(String key) {
        return remove(key, null);
    }

    /**
     * 获取配置 Map
     *
     * @param prefix  配置 Key 前置
     * @param profile 环境
     * @return Map
     */
    Map<String, String> getMapConfig(String prefix, String profile);

    /**
     * 获取配置 Map
     *
     * @param prefix 配置 Key 前置
     * @return Map
     */
    default Map<String, String> getMapConfig(String prefix) {
        return getMapConfig(prefix, null);
    }


    /**
     * 获取配置
     *
     * @param key     key
     * @param profile profile
     * @param clazz   class
     * @return object
     */
    default <T> T get(String key, String profile, Class<T> clazz) {
        String value = get(key, profile);
        if (Objects.isNull(value)) {
            return null;
        }
        if (TypeUtils.isString(clazz)) {
            return clazz.cast(value);
        }
        if (StrUtil.isBlank(value)) {
            return null;
        }
        if (TypeUtils.isSimple(clazz)) {
            return Convert.convert(clazz, value);
        }
        return JsonUtils.json(value, clazz);
    }


    /**
     * 获取配置
     *
     * @param key   key
     * @param clazz class
     * @return object
     */
    default <T> T get(String key, Class<T> clazz) {
        return get(key, null, clazz);
    }
}
