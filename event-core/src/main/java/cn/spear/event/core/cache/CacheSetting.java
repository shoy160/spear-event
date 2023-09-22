package cn.spear.event.core.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author shay
 * @date 2021/03/06
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheSetting {

    String subRegion() default "";

    int time() default 0;

    TimeUnit unit() default TimeUnit.SECONDS;
}
