package cn.spear.event.business.config;

import cn.spear.event.core.Constants;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Configuration
@EnableTransactionManagement
@MapperScan(Constants.MAPPER_PACKAGE)
@RequiredArgsConstructor
public class MybatisPlusAutoConfiguration {
    private final EventProperties config;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        interceptor.addInnerInterceptor(createDynamicInterceptor());
        return interceptor;
    }

    private InnerInterceptor createDynamicInterceptor() {
        DynamicTableNameInterceptor interceptor = new DynamicTableNameInterceptor();

        interceptor.setTableNameHandler((sql, tableName) -> {
            tableName = ReUtil.replaceAll(tableName, "[`\"]", Constants.STR_EMPTY);
            switch (config.getDbType()) {
                case POSTGRE_SQL:
                    tableName = String.format("\"%s\"", tableName);
                    break;
                case MYSQL:
                    tableName = String.format("`%s`", tableName);
                    break;
                default:
                    break;
            }
            return tableName;
        });
        interceptor.setSqlHandler(sql -> {
            switch (config.getDbType()) {
                case POSTGRE_SQL:
                    return sql.replaceAll("is_deleted=1", "is_deleted=TRUE");
                case MYSQL:
                    return sql.replaceAll("is_deleted=TRUE", "is_deleted=1");
                default:
                    return sql;
            }
        });
        return interceptor;
    }
}
