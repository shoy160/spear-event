package cn.spear.event.connector.kafka.consumer;

import cn.spear.event.core.connector.Consumer;
import cn.spear.event.core.connector.EventListener;
import cn.spear.event.core.connector.context.AbstractContext;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
public class KafkaConsumer implements Consumer {
    private KafkaConsumerAdapter adapter;

    @Override
    public synchronized void init(Properties properties) {
        if (null != this.adapter) {
            this.adapter.shutdown();
        }
        adapter = new KafkaConsumerAdapter(properties);
    }

    @Override
    public void updateOffset(List<CloudEvent> cloudEvents, AbstractContext context) {
        adapter.updateOffset(cloudEvents, context);
    }

    @Override
    public void subscribe(String topic) {
        adapter.subscribe(topic);
    }

    @Override
    public boolean isStarted() {
        return adapter.isStarted();
    }

    @Override
    public boolean isClosed() {
        return adapter.isClosed();
    }

    @Override
    public synchronized void start() {
        adapter.start();
    }

    @Override
    public void unsubscribe(String topic) {
        adapter.unsubscribe(topic);
    }

    @Override
    public void registerEventListener(EventListener listener) {
        adapter.registerEventListener(listener);
    }

    @Override
    public synchronized void shutdown() {
        adapter.shutdown();
    }
}
