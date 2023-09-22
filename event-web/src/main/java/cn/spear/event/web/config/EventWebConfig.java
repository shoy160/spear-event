package cn.spear.event.web.config;

import cn.spear.event.core.Constants;
import cn.spear.event.web.filter.AuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.spring.mvc.CloudEventHttpMessageConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author shay
 * @date 2020/9/1
 */
@Order(value = 1)
@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class EventWebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final BaseProperties config;
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .excludePathPatterns("/static/*", "/v2/api-docs", "/swagger-resources")
                .addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (!config.isEnableCors()) {
            return;
        }
        String corsPath = config.getCorsPath();
        String origin = config.getCorsOrigin();
        String[] headers = config.getCorsHeaders().split(Constants.STR_SPLIT);
        String[] methods = config.getCorsMethods().split(Constants.STR_SPLIT);
        registry.addMapping(corsPath)
                .allowCredentials(true)
                .allowedOriginPatterns(origin)
                .allowedMethods(methods)
                .allowedHeaders(headers);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) converter).setObjectMapper(this.objectMapper);
            }
        }
    }

    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        converters.add(0, new CloudEventHttpMessageConverter());
    }
}
