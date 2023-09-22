package cn.spear.event.web;

import cn.spear.event.core.AppContext;
import cn.spear.event.core.Constants;
import cn.spear.event.core.launcher.LauncherManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Properties;

/**
 * 应用启动类
 *
 * @author shay
 * @date 2020/7/15
 */
public class BaseApplication extends SpringApplication {

    private final String appName;

    public BaseApplication(String appName, Class<?>... primarySources) {
        super(primarySources);
        this.appName = appName;
    }

    @Override
    public ConfigurableApplicationContext run(String... args) {
        Assert.hasText(appName, "[appName]不能为空");
        AppContext.init(appName);
        LauncherManager manager = LauncherManager.getInstance();
        manager.preLoad();
        this.addListeners((ApplicationListener<ApplicationFailedEvent>) applicationEvent -> {
            manager.onDestroy();
        });
        this.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) applicationEvent -> {
            this.setBasicConfig(applicationEvent.getEnvironment());
        });
        ConfigurableApplicationContext context = super.run(args);
        manager.onLoad();
        return context;
    }

    public static ConfigurableApplicationContext run(String appName, Class<?> primarySource, String... args) {
        return run(appName, new Class[]{primarySource}, args);
    }

    public static ConfigurableApplicationContext run(String appName, Class<?>[] primarySources, String[] args) {
        return (new BaseApplication(appName, primarySources)).run(args);
    }

    private void setBasicConfig(ConfigurableEnvironment environment) {
        if (null == environment) {
            return;
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        final String basicName = "basicProperties";
        PropertySource<?> basic = propertySources.get(basicName);
        if (Objects.nonNull(basic)) {
            propertySources.remove(basicName);
        }
        Properties props = new Properties();
        props.setProperty("spring.application.name", this.appName);
        props.setProperty("spring.messages.encoding", "UTF-8");
        props.setProperty("authing.version", Constants.VERSION);
        //优雅停机
        props.setProperty("server.shutdown", "graceful");
        props.setProperty("spring.lifecycle.timeout-per-shutdown-phase", "20s");
        //开启健康检测
        props.setProperty("management.endpoint.health.probes.enabled", "true");
        LauncherManager.getInstance().config(environment, props);
        basic = new PropertiesPropertySource(basicName, props);
        propertySources.addLast(basic);
        AppContext.setAppMode(environment.getActiveProfiles());
    }

    @Override
    protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
        environment.setDefaultProfiles(Constants.MODE_DEV, Constants.PROFILE_SECRET);
        super.configureEnvironment(environment, args);
//        this.setBasicConfig(environment);
    }
}
