package cn.spear.event.core.utils;

import cn.spear.event.core.Constants;
import cn.spear.event.core.lang.Func;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo
 *
 * @author shay
 * @date 2020/8/15
 */
public class MapUtils {

    public static Map<String, Object> map(Object obj) {
        return map(obj, null);
    }

    public static Map<String, Object> map(Object obj, Func<String, String> keyEditor) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        try {
            if (obj instanceof Map) {
                Map<?, ?> item = (Map<?, ?>) obj;
                for (Object key : item.keySet()) {
                    String mapKey = key.toString();
                    if (null != keyEditor) {
                        mapKey = keyEditor.invoke(mapKey);
                    }
                    if (StrUtil.isBlank(mapKey)) {
                        continue;
                    }
                    map.put(mapKey, item.get(key));
                }
                return map;
            }
            return BeanUtil.beanToMap(obj, new LinkedHashMap<>(), false, t -> {
                if (null != keyEditor) {
                    return keyEditor.invoke(t);
                }
                return t;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return map;
        }
    }

    public static String toUrl(Map<String, Object> map) {
        return toUrl(map, Constants.STR_EMPTY, true);
    }

    public static String toUrl(Map<String, Object> map, String charset) {
        return toUrl(map, charset, true);
    }


    public static String toUrl(Map<String, Object> map, String charset, boolean filterEmpty) {
        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            if (filterEmpty && CommonUtils.isEmpty(key)) {
                continue;
            }
            String value = CommonUtils.unEscape(map.get(key));
            if (value == null) {
                if (filterEmpty) {
                    continue;
                }
                value = Constants.STR_EMPTY;
            } else {
                if (CommonUtils.isNotEmpty(charset)) {
                    try {
                        value = URLEncoder.encode(value, charset);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            builder.append(String.format("%s=%s&", key, value));
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    public static <T> T getValueByPath(Map<String, Object> map, Class<T> clazz, String paths) {
        return getValue(map, clazz, paths.split("\\."));
    }

    public static <T> T getValue(Map<String, Object> map, Class<T> clazz, String... paths) {
        if (MapUtil.isEmpty(map)) {
            return null;
        }
        Pattern pattern = PatternPool.get("\\[(\\d+)\\]$", Pattern.DOTALL);
        Object currentValue = map;
        for (String path : paths) {
            if (!(currentValue instanceof Map)) {
                return null;
            }
            Map<String, Object> currentMap = Convert.toMap(String.class, Object.class, currentValue);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                //下标处理
                int index = Convert.toInt(matcher.group(1));
                String key = path.replace(matcher.group(0), Constants.STR_EMPTY);
                Object value = currentMap.get(key);
                if (!TypeUtils.isArray(value)) {
                    return null;
                }
                currentValue = ((ArrayList<?>) value).get(index);
                continue;
            }
            currentValue = currentMap.get(path);
        }
        return Objects.isNull(currentValue) ? null : Convert.convert(clazz, currentValue);
    }

}
