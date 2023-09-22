package cn.spear.event.business.config;

import cn.spear.event.core.lang.Func;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2023/3/7
 */
public class DynamicTableNameInterceptor extends DynamicTableNameInnerInterceptor {
    private Func<String, String> sqlHandler;

    public void setSqlHandler(Func<String, String> sqlHandler) {
        this.sqlHandler = sqlHandler;
    }

    @Override
    public void beforeQuery(
            Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql
    ) throws SQLException {
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
        String sql = this.changeTable(mpBs.sql());
        if (Objects.nonNull(sqlHandler)) {
            sql = sqlHandler.invoke(sql);
        }
        mpBs.sql(sql);
    }
}
