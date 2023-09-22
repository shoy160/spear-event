package cn.spear.event.business.dao.handler;

import cn.spear.event.core.Constants;
import cn.spear.event.core.utils.JsonUtils;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoy
 * @date 2021/7/1
 */
@MappedTypes({List.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.OTHER})
public class JsonListTypeHandler<T> extends AbstractJsonTypeHandler<List<T>> {
    private final Class<T> type;

    public JsonListTypeHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, toJson(parameter));
    }

    @Override
    protected List<T> parse(String json) {
        return StrUtil.isBlank(json) ? new ArrayList<>() : JsonUtils.jsonList(json, type);
    }

    @Override
    protected String toJson(List<T> obj) {
        return null == obj ? Constants.STR_EMPTY : JsonUtils.toJson(obj);
    }
}
