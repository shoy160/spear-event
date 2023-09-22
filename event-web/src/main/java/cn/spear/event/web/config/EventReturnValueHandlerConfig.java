package cn.spear.event.web.config;

import cn.spear.event.web.handler.EventReturnValueHandler;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Configuration
public class EventReturnValueHandlerConfig implements InitializingBean {

    private RequestMappingHandlerAdapter handlerAdapter;
    private final boolean isJackson;

    @Autowired
    @SuppressWarnings("all")
    public void setHandlerAdapter(RequestMappingHandlerAdapter handlerAdapter) {
        this.handlerAdapter = handlerAdapter;
    }

    public EventReturnValueHandlerConfig() {
        ClassLoader classLoader = WebMvcConfigurationSupport.class.getClassLoader();
        isJackson = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader)
                && ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = this.handlerAdapter.getReturnValueHandlers();
        if (CollUtil.isEmpty(returnValueHandlers)) {
            return;
        }
        List<HandlerMethodReturnValueHandler> handlers = returnValueHandlers
                .stream()
                .map(t -> {
                    if (t instanceof RequestResponseBodyMethodProcessor) {
                        List<Object> bodyAdvices = new ArrayList<>();
                        if (this.isJackson) {
                            bodyAdvices.add(new JsonViewRequestBodyAdvice());
                            bodyAdvices.add(new JsonViewResponseBodyAdvice());
                        }
                        return new EventReturnValueHandler(this.handlerAdapter.getMessageConverters(), new ContentNegotiationManager(), bodyAdvices);
                    }
                    return t;
                }).collect(Collectors.toList());
        this.handlerAdapter.setReturnValueHandlers(handlers);
    }
}
