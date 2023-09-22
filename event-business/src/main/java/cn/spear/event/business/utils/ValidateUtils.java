package cn.spear.event.business.utils;

import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.CommonUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 校验工具类
 *
 * @author shay
 * @date 2020/11/11
 */
public class ValidateUtils {

    /**
     * 实体校验
     *
     * @param obj        obj
     * @param clazzArray class list
     */
    public static void validate(Object obj, Class<?>... clazzArray) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> errors = validator.validate(obj, clazzArray);
        if (errors.size() == 0) {
            return;
        }
        List<String> errorList = new ArrayList<>();
        for (ConstraintViolation<Object> error : errors) {
            String fieldName = error.getPropertyPath().toString();
            Field field = ReflectUtil.getField(obj.getClass(), fieldName);
            if (field != null) {
                fieldName = CommonUtils.getDesc(field, obj.getClass());
            }
            String message = error.getMessage();
            errorList.add(fieldName.concat(message));
        }
        throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), String.join(";", errorList));
    }

    /**
     * 断言 - Null
     *
     * @param value   value
     * @param message message
     */
    public static void isNull(Object value, String message) {
        if (value == null) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - 非Null
     *
     * @param value   value
     * @param message message
     */
    public static void isNotNull(Object value, String message) {
        if (value != null) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - 空
     *
     * @param value   value
     * @param message message
     */
    public static void isEmpty(Object value, String message) {
        if (ObjectUtil.isEmpty(value)) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - 非空
     *
     * @param value   value
     * @param message message
     */
    public static void isNotEmpty(Object value, String message) {
        if (ObjectUtil.isNotEmpty(value)) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - True
     *
     * @param value   value
     * @param message message
     */
    public static void isTrue(Boolean value, String message) {
        if (value != null && value) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - True
     *
     * @param value   value
     * @param message message
     */
    public static void isTrue(boolean value, String message) {
        if (value) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - False
     *
     * @param value   value
     * @param message message
     */
    public static void isFalse(Boolean value, String message) {
        if (value == null || !value) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - False
     *
     * @param value   value
     * @param message message
     */
    public static void isFalse(boolean value, String message) {
        if (!value) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - 相等
     *
     * @param value   value
     * @param source  source
     * @param message message
     */
    public static void isEquals(Object value, Object source, String message) {
        if ((value == null)) {
            if (source == null) {
                return;
            }
        } else if (value.equals(source)) {
            return;
        }
        throw new BusinessException(message);
    }

    /**
     * 断言 - 不相等
     *
     * @param value   value
     * @param source  source
     * @param message message
     */
    public static void isNotEquals(Object value, Object source, String message) {
        if ((value == null)) {
            if (source != null) {
                return;
            }
        } else if (!value.equals(source)) {
            return;
        }
        throw new BusinessException(message);
    }
}
