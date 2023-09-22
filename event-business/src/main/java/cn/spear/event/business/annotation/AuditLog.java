package cn.spear.event.business.annotation;

import cn.spear.event.business.domain.enums.AuditOperationType;
import cn.spear.event.business.domain.enums.AuditResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 资源类别
     *
     * @return type
     */
    AuditResourceType resource();

    /**
     *  操作类别
     *
     * @return type
     */
    AuditOperationType operation();
}
