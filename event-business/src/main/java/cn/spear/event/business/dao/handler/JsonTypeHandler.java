package cn.spear.event.business.dao.handler;

import cn.spear.event.core.utils.JsonUtils;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * Json 类型处理器
 *
 * @author shoy
 * @date 2021/6/30
 */
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.OTHER})
public class JsonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private final Class<?> type;

    public JsonTypeHandler(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    protected Object parse(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JsonUtils.json(json, this.type);
    }

    @Override
    protected String toJson(Object obj) {
        if (null == obj) {
            return null;
        }
        return JsonUtils.toJson(obj);
    }
}
