package cn.spear.event.business.cache;

import cn.spear.event.core.lang.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis过期监听事件
 *
 * @author shay
 * @date 2021/2/25
 */
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final List<Action<String>> expiredAction;

    public RedisKeyExpirationListener(RedisMessageListenerContainer container) {
        super(container);
        expiredAction = new ArrayList<>();
    }

    public void setAction(Action<String> action) {
        expiredAction.add(action);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message message
     * @param pattern pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("redis key expired ->> " + expiredKey);
        for (Action<String> act : expiredAction) {
            act.invoke(expiredKey);
        }
    }
}
