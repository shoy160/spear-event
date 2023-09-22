package cn.spear.event.core.annotation;

import cn.spear.event.core.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAuth {
    /**
     * 允许匿名
     *
     * @return boolean
     */
    boolean anonymous() default false;

    /**
     * 类型
     *
     * @return string
     */
    String group() default Constants.STR_EMPTY;

    /**
     * 权限列表(任一)
     *
     * @return roles
     */
    String[] roles() default {};
}
