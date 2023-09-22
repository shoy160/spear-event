package cn.spear.event.business.launcher;

import cn.spear.event.core.launcher.Launcher;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

/**
 * Mybatisplus 加载器
 *
 * @author luoyong
 * @date 2023/3/8
 */
@Slf4j
public class MybatisPlusLauncher implements Launcher {
    private static final String DB_CONFIG = "mybatis-plus.global-config.db-config";
    private static final String DB_TYPE_PROPERTY = "spear.event.db-type";
    private static final String PROP_DRIVER_CLASS = "spring.datasource.driver-class-name";
    private static final String PROP_COLUMN_FORMAT = "column-format";
    private static final String PROP_LOGIC_DELETE_VALUE = "logic-delete-value";
    private static final String PROP_LOGIC_NOT_DELETE_VALUE = "logic-not-delete-value";
    private static final String PROP_FLYWAY_LOCATIONS = "spring.flyway.locations";

    private void setDbConfig(Properties props, String key, String value) {
        props.setProperty(String.format("%s.%s", DB_CONFIG, key), value);
    }

    @Override
    public void config(ConfigurableEnvironment environment, Properties props) {
        String dbType =
                environment.resolvePlaceholders(String.format("${%s:}", DB_TYPE_PROPERTY));
        if (StrUtil.isBlank(dbType)) {
            String driverClass =
                    environment.resolvePlaceholders(String.format("${%s:}", PROP_DRIVER_CLASS));
            if (StrUtil.isNotBlank(driverClass)) {
                Optional<DbType> dbTypeOptional = Arrays.stream(DbType.values())
                        .filter(t -> StrUtil.containsIgnoreCase(driverClass, t.getDb()))
                        .findFirst();
                if (dbTypeOptional.isPresent()) {
                    dbType = dbTypeOptional.get().name();
                }
                props.setProperty(DB_TYPE_PROPERTY, dbType);
            }
        }
        if (StrUtil.isBlank(dbType)) {
            log.warn("未找到数据库配置，取消 Migration 操作");
            props.setProperty("spring.flyway.enabled", "false");
            return;
        }

        log.info("Config For DbType -> {}", dbType);
        props.setProperty("spring.flyway.enabled", "true");
        props.setProperty("spring.flyway.clean-disabled", "true");
        props.setProperty("spring.flyway.baseline-on-migrate", "true");
        props.setProperty("spring.flyway.validate-on-migrate", "false");
        props.setProperty("spring.flyway.placeholder-replacement", "false");
        props.setProperty("spring.flyway.table", "migration_history");
        switch (DbType.valueOf(dbType)) {
            case MYSQL:
                setDbConfig(props, PROP_COLUMN_FORMAT, "`%s`");
                setDbConfig(props, PROP_LOGIC_DELETE_VALUE, "1");
                setDbConfig(props, PROP_LOGIC_NOT_DELETE_VALUE, "0");
                props.setProperty(PROP_FLYWAY_LOCATIONS, "classpath:/db/migration/mysql");
                break;
            case POSTGRE_SQL:
                setDbConfig(props, PROP_COLUMN_FORMAT, "\"%s\"");
                setDbConfig(props, PROP_LOGIC_DELETE_VALUE, "TRUE");
                setDbConfig(props, PROP_LOGIC_NOT_DELETE_VALUE, "FALSE");
                props.setProperty(PROP_FLYWAY_LOCATIONS, "classpath:/db/migration/pgsql");
                break;
            default:
                break;
        }
    }
}
