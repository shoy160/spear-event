package cn.spear.event.business.cache;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启Redis缓存过期监听
 *
 * @author shay
 * @date 2021/03/06
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RedisKeyExpirationListener.class})
public @interface EnableRedisExpireListener {
}
