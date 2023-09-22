package cn.spear.event.web.advice;

import cn.spear.event.core.AppContext;
import cn.spear.event.core.Constants;
import cn.spear.event.core.domain.dto.ResultDTO;
import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.exception.BusinessException;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Locale;

/**
 * Todo
 *
 * @author shay
 * @date 2020/7/15
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionResolver {
    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 未知异常处理
     *
     * @param ex 异常
     * @return ResponseEntity
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ResultDTO<?> result;
        if (AppContext.isProd()) {
            result = ResultDTO.fail(ResultCode.INTERNAL_SERVER_ERROR);
        } else {
            result = ResultDTO.fail(ResultCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(result);
    }

    /**
     * 多参数校验异常信息处理
     *
     * @param fieldErrors errors
     * @return error message
     */
    private String processFieldErrors(List<FieldError> fieldErrors) {
        if (ArrayUtil.isEmpty(fieldErrors)) {
            return Constants.STR_EMPTY;
        }
        if (AppContext.isProd()) {
            return resolveLocalizedErrorMessage(fieldErrors.get(0));
        }
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            String localizedErrorMessage = resolveLocalizedErrorMessage(fieldError);
            sb.append(localizedErrorMessage);
            sb.append(";");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private String resolveLocalizedErrorMessage(FieldError fieldError) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(fieldError, currentLocale);
    }

    /**
     * 处理参数校验异常
     *
     * @param ex ex
     * @return ResponseEntity
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleArgException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        final String message = processFieldErrors(fieldErrors);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResultDTO.fail(ResultCode.PARAM_VALID_ERROR, message));
    }

    /**
     * 参数缺少异常处理
     *
     * @param ex 异常
     * @return ResponseEntity
     */
    @ResponseBody
    @ExceptionHandler({MissingServletRequestParameterException.class,})
    public ResponseEntity<Object> handleMissingException(MissingServletRequestParameterException ex) {
        String message = ex.getLocalizedMessage();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResultDTO.fail(ResultCode.PARAM_MISS, message));
    }

    /**
     * 参数校验
     *
     * @param ex ex
     * @return ResponseEntity
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleArgException(BindException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        final String message = processFieldErrors(fieldErrors);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResultDTO.fail(ResultCode.PARAM_VALID_ERROR, message));
    }

    /**
     * 业务异常处理
     *
     * @param ex 业务异常
     * @return ResponseEntity
     */
    @ResponseBody
    @ExceptionHandler({BusinessException.class,})
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        log.warn(String.format("业务异常：%d,%s", ex.getCode(), ex.getMessage()));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResultDTO.fail(ex.getCode(), ex.getMessage()));
    }
}
