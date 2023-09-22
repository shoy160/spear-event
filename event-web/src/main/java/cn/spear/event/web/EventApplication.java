package cn.spear.event.web;

import cn.spear.event.core.Constants;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author luoyong
 * @date 2022/11/7
 */
@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE)
public class EventApplication {
    public static void main(String[] args) {
        BaseApplication.run(Constants.APP_NAME, EventApplication.class, args);
    }
}
