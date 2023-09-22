package cn.spear.event.core.connector;

import cn.spear.event.core.connector.enums.ConsumerResetType;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.lang.Action;

import java.util.Properties;

/**
 * @author luoyong
 * @date 2022/11/9
 */
public interface ConnectorManager {
    /**
     * 创建消费者
     *
     * @param group        消费组
     * @param username     用户名
     * @param password     密码
     * @param resetType    重置策略
     * @param configAction 配置操作
     * @return Consumer
     */
    Consumer createConsumer(String group, String username, String password, ConsumerResetType resetType, Action<Properties> configAction);

    /**
     * 创建消费者
     *
     * @param group        消费组
     * @param username     用户名
     * @param password     密码
     * @param configAction 配置操作
     * @return Consumer
     */
    default Consumer createConsumer(String group, String username, String password, Action<Properties> configAction) {
        return createConsumer(group, username, password, null, configAction);
    }

    /**
     * 创建消费者
     *
     * @param group     消费组
     * @param username  用户名
     * @param password  密码
     * @param resetType 重置策略
     * @return Consumer
     */
    default Consumer createConsumer(String group, String username, String password, ConsumerResetType resetType) {
        return createConsumer(group, username, password, resetType, null);
    }

    /**
     * 创建生产者
     *
     * @param username 用户名
     * @param password 密码
     * @return Producer
     */
    Producer createProducer(String username, String password);

    /**
     * 创建消费者
     *
     * @param group    消费组
     * @param username 用户名
     * @param password 密码
     * @return Consumer
     */
    default Consumer createConsumer(String group, String username, String password) {
        return createConsumer(group, username, password, null, null);
    }


    /**
     * 生成消费配置
     *
     * @param group    消费组
     * @param username 用户名
     * @param password 密码
     * @param language 编程语言
     * @return 消费配置
     */
    Properties createConsumerConfig(String group, LanguageType language, String username, String password);

    /**
     * 创建管理员生产者
     *
     * @return Producer
     */
    Producer createEventProducer();

    /**
     * 创建管理员消费者
     *
     * @param group     消费组
     * @param username  用户名
     * @param password  密码
     * @param resetType 重置策略
     * @return Consumer
     */
    Properties createConsumerConfig(String group, String username, String password, ConsumerResetType resetType);
}
