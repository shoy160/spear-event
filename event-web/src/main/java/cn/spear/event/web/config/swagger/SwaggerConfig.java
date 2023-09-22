package cn.spear.event.web.config.swagger;

import cn.spear.event.core.Constants;
import cn.spear.event.web.config.BaseProperties;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author luoyong
 * @date 2021/5/28
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig extends BaseSwaggerConfig {
    public SwaggerConfig(BaseProperties properties) {
        super(properties);
    }

    private String getPackage(String name) {
        return String.format("%s.%s", Constants.REST_PACKAGE, name);
    }

    @Bean
    public Docket appDocket() {
        return getDocket("APP - 事件中台", Constants.VERSION, getPackage(Constants.GROUP_APP), Constants.GROUP_APP);
    }

    @Bean
    public Docket manageDocket() {
        return getDocket("管理后台 - 事件中台", Constants.VERSION, getPackage(Constants.GROUP_MANAGE), Constants.GROUP_MANAGE);
    }
}
