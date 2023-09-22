package cn.spear.event.web.handler;

import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * @author luoyong
 * @date 2023/2/24
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(ResponseBody.class)
public @interface ResponseRaw {
}
