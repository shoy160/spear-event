package cn.spear.event.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author luoyong
 * @date 2022/12/28
 */
@Component
@SuppressWarnings("rawtypes")
@ConditionalOnProperty(name = "spear.enable-history", havingValue = "true")
public class EventWebConfiguration implements WebServerFactoryCustomizer {
    @Override
    public void customize(WebServerFactory factory) {
        ((ErrorPageRegistry) factory).addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/index.html"));
    }
}
