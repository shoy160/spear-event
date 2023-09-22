package cn.spear.event.business.service.impl;

import cn.spear.event.business.dao.ConfigMapper;
import cn.spear.event.business.domain.dto.ConfigDTO;
import cn.spear.event.business.domain.po.ConfigPO;
import cn.spear.event.business.service.ConfigService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.AppContext;
import cn.spear.event.core.Constants;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.core.utils.TypeUtils;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author leizeyu
 * @date 2023/3/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigPO> implements ConfigService {

    private static final String PROFILE_DEFAULT = "default";
    private final Snowflake snowflake;
    private final Cache<String, String> cache;

    @Override
    public PagedDTO<ConfigDTO> page(int page, int size, String key, String prefix, String profile) {
        LambdaQueryChainWrapper<ConfigPO> wrapper = lambdaQuery()
                .eq(StrUtil.isNotBlank(key), ConfigPO::getKey, key)
                .likeRight(StrUtil.isNotBlank(prefix), ConfigPO::getKey, prefix);
        if (StrUtil.isNotBlank(profile)) {
            if (PROFILE_DEFAULT.equals(profile)) {
                wrapper.isNull(ConfigPO::getProfile);
            } else {
                wrapper.eq(ConfigPO::getProfile, profile);
            }
        }
        Page<ConfigPO> paged = wrapper.orderByDesc(ConfigPO::getId)
                .page(new Page<>(page, size));
        return PagedUtils.convert(paged, ConfigDTO.class);
    }

    @Override
    public List<String> allKeys(String prefix, String profile) {
        profile = resolveProfile(profile);
        LambdaQueryChainWrapper<ConfigPO> wrapper = lambdaQuery()
                .eq(ConfigPO::getProfile, profile)
                .or().isNull(ConfigPO::getProfile);
        if (StrUtil.isNotBlank(prefix)) {
            wrapper.likeRight(ConfigPO::getKey, prefix);
        }
        return wrapper
                .select(ConfigPO::getKey)
                .list()
                .stream()
                .map(ConfigPO::getKey)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(String key, Object value, String profile) {
        if (StrUtil.isBlank(key)) {
            throw new BusinessException("配置 key 不能为空");
        }
        if (!ReUtil.isMatch("^[a-z][0-9a-z_-]*(\\.[a-z][0-9a-z_-]*)*$", key)) {
            throw new BusinessException("配置 key 命名不规范");
        }
        if (Objects.isNull(value)) {
            throw new BusinessException("配置值不能为空");
        }
        profile = resolveProfile(profile);
        Class<?> valueClass = value.getClass();
        String className = valueClass.getName();
        String strValue = TypeUtils.isSimple(value)
                ? value.toString()
                : JsonUtils.toJson(value);
        LambdaQueryChainWrapper<ConfigPO> wrapper = lambdaQuery().eq(ConfigPO::getKey, key);
        if (Objects.isNull(profile)) {
            wrapper.isNull(ConfigPO::getProfile);
        } else {
            wrapper.eq(ConfigPO::getProfile, profile);
        }
        ConfigPO model = wrapper
                .select(ConfigPO::getId)
                .last("LIMIT 1")
                .one();
        boolean result;
        if (Objects.isNull(model)) {
            model = new ConfigPO();
            model.setId(snowflake.nextId());
            model.setKey(key);
            model.setProfile(profile);
            model.setValue(strValue);
            model.setValueType(className);
            result = save(model);
        } else {
            result = lambdaUpdate()
                    .eq(ConfigPO::getId, model.getId())
                    .set(ConfigPO::getValue, strValue)
                    .set(ConfigPO::getValueType, className)
                    .update(model);
        }
        if (result) {
            cache.remove(cacheKey(key, profile));
        }
        return result;
    }

    @Override
    public String get(String key, String profile) {
        profile = resolveProfile(profile);
        String cacheKey = cacheKey(key, profile);
        String value = cache.get(cacheKey);
        if (Objects.isNull(value)) {
            ConfigPO config = getConfig(key, profile);
            if (Objects.isNull(config)) {
                value = Constants.STR_EMPTY;
            } else {
                value = config.getValue();
            }
            cache.put(cacheKey, value);
        }
        return value;
    }

    @Override
    public Object getObject(String key, String profile) {
        ConfigPO config = getConfig(key, profile);
        if (Objects.isNull(config)) {
            return null;
        }
        if (StrUtil.isNotBlank(config.getValueType())) {
            Class<?> valueClass;
            try {
                valueClass = Class.forName(config.getValueType());
            } catch (ClassNotFoundException e) {
                log.warn("配置值类型转换失败", e);
                return config.getValue();
            }
            if (TypeUtils.isSimple(valueClass)) {
                return Convert.convert(valueClass, config.getValue());
            } else {
                return JsonUtils.json(config.getValue(), valueClass);
            }
        }
        return config.getValue();
    }

    @Override
    public boolean remove(String key, String profile) {
        profile = resolveProfile(profile);
        boolean result = lambdaUpdate().eq(ConfigPO::getKey, key)
                .eq(ConfigPO::getProfile, profile)
                .remove();
        if (result) {
            cache.remove(cacheKey(key, profile));
        }
        return result;
    }

    @Override
    public Map<String, String> getMapConfig(String prefix, String profile) {
        profile = resolveProfile(profile);
        final String finalProfile = profile;
        List<ConfigPO> models = lambdaQuery()
                .likeRight(StrUtil.isNotBlank(prefix), ConfigPO::getKey, prefix)
                .and(t -> t.eq(ConfigPO::getProfile, finalProfile).or().isNull(ConfigPO::getProfile))
                .select(ConfigPO::getKey, ConfigPO::getValue)
                .list();
        Map<String, String> configMap = new HashMap<>(models.size());
        for (ConfigPO model : models) {
            String key = model.getKey();
            if (StrUtil.isNotBlank(prefix)) {
                key = key.replaceAll(prefix, Constants.STR_EMPTY);
                key = key.replaceAll("^\\.", Constants.STR_EMPTY);
            }
            if (configMap.containsKey(key) && StrUtil.isBlank(model.getProfile())) {
                continue;
            }
            configMap.put(key, model.getValue());
        }
        return configMap;
    }

    private ConfigPO getConfig(String key, String profile) {
        profile = resolveProfile(profile);
        if (StrUtil.isBlank(profile)) {
            profile = PROFILE_DEFAULT;
        }
        return baseMapper.queryConfig(key, profile);
    }

    private String cacheKey(String key, String profile) {
        if (PROFILE_DEFAULT.equals(profile) || StrUtil.isBlank(profile)) {
            return key;
        }
        return String.format("%s_%s", key, profile);
    }

    private String resolveProfile(String profile) {
        if (Objects.equals(PROFILE_DEFAULT, profile)) {
            return null;
        }
        if (!Constants.validMode(profile)) {
            profile = AppContext.getAppMode();
        }
        return profile;
    }
}
