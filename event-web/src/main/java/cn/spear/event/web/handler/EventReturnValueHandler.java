package cn.spear.event.web.handler;

import cn.spear.event.core.domain.dto.ResultDTO;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/8
 */
public class EventReturnValueHandler extends RequestResponseBodyMethodProcessor
        implements HandlerMethodReturnValueHandler {

    public EventReturnValueHandler(
            List<HttpMessageConverter<?>> converters, @Nullable ContentNegotiationManager manager,
            @Nullable List<Object> requestResponseBodyAdvice
    ) {
        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsReturnType(@NonNull MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(
            Object returnValue, @NonNull MethodParameter returnType,
            @NonNull ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest
    ) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Method method = returnType.getMethod();
        if (Objects.nonNull(method)) {
            ResponseRaw responseRaw = method.getAnnotation(ResponseRaw.class);
            if (Objects.nonNull(responseRaw)) {
                super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
                return;
            }
        }

        Object value = returnValue instanceof ResultDTO ? returnValue : ResultDTO.success(returnValue);
        super.handleReturnValue(value, returnType, mavContainer, webRequest);
    }
}
