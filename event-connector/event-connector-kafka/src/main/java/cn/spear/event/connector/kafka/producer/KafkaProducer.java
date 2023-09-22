package cn.spear.event.connector.kafka.producer;

import cn.spear.event.core.connector.Producer;
import cn.spear.event.core.connector.callback.ReplyCallback;
import cn.spear.event.core.connector.callback.SendCallback;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Kafka Producer
 *
 * @author luoyong
 * @date 2022/11/9
 */
@RequiredArgsConstructor
public class KafkaProducer implements Producer {
    private KafkaProducerAdapter adapter;

    @Override
    public synchronized void init(Properties properties) {
        if (null != this.adapter) {
            this.adapter.shutdown();
        }
        this.adapter = new KafkaProducerAdapter(properties);
    }

    @Override
    public boolean isStarted() {
        return this.adapter.isStarted();
    }

    @Override
    public boolean isClosed() {
        return this.adapter.isClosed();
    }

    @Override
    public void start() {
        this.adapter.start();
    }

    @Override
    public void shutdown() {
        this.adapter.shutdown();
    }


    @Override
    public void publish(CloudEvent cloudEvent, SendCallback sendCallback) {
        this.adapter.sendAsync(cloudEvent, sendCallback);
    }

    @Override
    public void sendOneway(CloudEvent cloudEvent) {
        this.adapter.sendOneway(cloudEvent);
    }

    @Override
    public void request(CloudEvent cloudEvent, ReplyCallback replyCallback, long timeout) {
        this.adapter.request(cloudEvent, replyCallback, timeout);
    }

    @Override
    public boolean reply(CloudEvent cloudEvent, SendCallback sendCallback) {
        this.adapter.reply(cloudEvent, sendCallback);
        return false;
    }

    @Override
    public void checkTopicExist(String topic) throws Exception {
        this.adapter.checkTopicExist(topic);
    }

    @Override
    public void setExtFields() {

    }
}
