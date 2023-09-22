package cn.spear.event.web.config;

import cn.spear.event.core.Constants;
import cn.spear.event.core.session.Session;
import cn.spear.event.core.session.TenantSolver;
import cn.spear.event.web.security.EventWebContext;
import cn.spear.event.web.security.ServletSession;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Component
public class EventAutoConfiguration {

    @Bean
    private Snowflake snowflake() {
        DateTime date = DateUtil.parseDate("2022-11-1");
        return new Snowflake(date, 1, 1, true);
    }

    @Bean
    @ConditionalOnMissingBean
    public Session getSession(TenantSolver tenantSolver) {
        return new ServletSession(tenantSolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantSolver tenantSolver() {
        return () -> {
            HttpServletRequest request = EventWebContext.getRequest();
            if (request == null) {
                return null;
            }
            return request.getParameter(Constants.HEADER_TENANT_ID);
        };
    }
}
