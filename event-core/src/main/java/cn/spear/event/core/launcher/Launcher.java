package cn.spear.event.core.launcher;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 应用加载接口
 *
 * @author shay
 * @date 2020/8/14
 */
public interface Launcher {
    /**
     * 预加载
     */
    default void preLoad() {
    }

    /**
     * 初始化配置
     *
     * @param environment 当前配置项
     * @param properties  基础配置项
     */
    default void config(ConfigurableEnvironment environment, Properties properties) {

    }

    /**
     * 加载
     */
    default void onLoad() {
    }

    /**
     * 销毁
     */
    default void onDestroy() {

    }
}
