package cn.spear.event.business.dao.handler;

import cn.spear.event.core.enums.BaseEnum;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Mybatis 枚举转换
 *
 * @author luoyong
 * @date 2022/11/12
 **/
public class MybatisEnumTypeHandler<E extends Enum<E>>
        extends com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler<E> {
    private final Class<E> clazz;

    public MybatisEnumTypeHandler(Class<E> enumClassType) {
        super(enumClassType);
        this.clazz = enumClassType;
    }

    private BaseEnum convert(E value) {
        return value instanceof BaseEnum ? (BaseEnum) value : null;
    }

    private boolean isBaseEnum() {
        return BaseEnum.class.isAssignableFrom(clazz);
    }

    public E getEnum(int value) {
        for (E item : clazz.getEnumConstants()) {
            if (item instanceof BaseEnum && Objects.equals(((BaseEnum) item).getValue(), value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        BaseEnum baseEnum = convert(parameter);
        if (null != baseEnum) {
            ps.setInt(i, baseEnum.getValue());
        } else {
            super.setNonNullParameter(ps, i, parameter, jdbcType);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (isBaseEnum()) {
            int value = rs.getInt(columnIndex);
            return getEnum(value);
        }
        return super.getNullableResult(rs, columnIndex);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (isBaseEnum()) {
            int value = rs.getInt(columnName);
            return getEnum(value);
        }
        return super.getNullableResult(rs, columnName);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (isBaseEnum()) {
            int value = cs.getInt(columnIndex);
            return getEnum(value);
        }
        return super.getNullableResult(cs, columnIndex);
    }
}
