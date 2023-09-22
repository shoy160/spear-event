package cn.spear.event.connector.kafka.config;

import cn.spear.event.connector.kafka.delay.KafkaDelayManager;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luoyong
 * @date 2022/11/30
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KafkaDelayManager.class})
public @interface EnableDelay {
}
