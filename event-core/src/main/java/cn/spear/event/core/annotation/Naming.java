package cn.spear.event.core.annotation;

import cn.spear.event.core.enums.NamingType;

import java.lang.annotation.*;

/**
 * 命名注解
 *
 * @author shay
 * @date 2020/11/11
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Naming {
    /**
     * 名称
     */
    String name() default "";

    /**
     * 描述
     */
    String desc() default "";

    /**
     * 命名方式
     */
    NamingType type() default NamingType.Normal;

    /**
     * 前缀
     */
    String prefix() default "";

    /**
     * 后缀
     */
    String suffix() default "";
}
