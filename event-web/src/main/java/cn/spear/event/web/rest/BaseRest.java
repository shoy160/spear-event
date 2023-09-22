package cn.spear.event.web.rest;

import cn.spear.event.core.domain.dto.ResultDTO;
import cn.spear.event.core.enums.ResultCode;
import cn.spear.event.core.utils.CommonUtils;

import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/7
 */
public abstract class BaseRest {
    protected ResultDTO success() {
        return ResultDTO.success(new Object());
    }

    protected <T> ResultDTO<T> success(T data) {
        return ResultDTO.success(data);
    }

    protected <T> ResultDTO<T> fail(ResultCode resultCode) {
        return ResultDTO.failT(resultCode);
    }

    protected <T> ResultDTO<T> fail(String message) {
        return ResultDTO.fail(ResultCode.FAILURE, message);
    }

    protected <T> ResultDTO<T> fail(String message, int code) {
        return ResultDTO.failT(code, message);
    }

    protected <T> ResultDTO<T> result(boolean status, String errorMsg) {
        return status ? success() : fail(errorMsg);
    }

    protected <T> T toBean(Object source, Class<T> clazz) {
        return CommonUtils.toBean(source, clazz);
    }
    
    protected <T, TS> List<T> toListBean(List<TS> sourceList, Class<T> clazz) {
        return CommonUtils.toListBean(sourceList, clazz);
    }
}
