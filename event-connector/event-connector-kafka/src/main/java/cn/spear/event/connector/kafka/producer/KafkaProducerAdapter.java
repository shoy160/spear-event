/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.spear.event.connector.kafka.producer;

import cn.hutool.core.util.StrUtil;
import cn.spear.event.core.connector.callback.NullSendCallback;
import cn.spear.event.core.connector.callback.ReplyCallback;
import cn.spear.event.core.connector.callback.SendCallback;
import cn.spear.event.core.domain.event.SendResult;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka Adapter
 *
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
public class KafkaProducerAdapter {
    private final KafkaProducer<String, CloudEvent> producer;
    private final Properties properties;

    private final AtomicBoolean isStarted;

    public KafkaProducerAdapter(Properties props) {
        this.isStarted = new AtomicBoolean(false);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        this.properties = props;
        this.producer = new KafkaProducer<>(props);
    }

    public boolean isStarted() {
        return isStarted.get();
    }

    public boolean isClosed() {
        return !isStarted.get();
    }

    public void start() {
        isStarted.compareAndSet(false, true);
    }

    public void shutdown() {
        isStarted.compareAndSet(true, false);
    }

    public KafkaProducerAdapter init(Properties properties) throws Exception {
        return new KafkaProducerAdapter(properties);
    }

    public void send(CloudEvent cloudEvent) {
        sendAsync(cloudEvent, NullSendCallback.instance());
    }

    public void checkTopicExist(String topic) throws ExecutionException, InterruptedException, ConnectorRuntimeException {
        Admin admin = Admin.create(properties);
        Set<String> topicNames = admin.listTopics().names().get();
        admin.close();
        boolean exist = topicNames.contains(topic);
        if (!exist) {
            throw new ConnectorRuntimeException(String.format("topic:%s is not exist", topic));
        }
    }

    public void request(CloudEvent cloudEvent, ReplyCallback replyCallback, long timeout) {
        throw new ConnectorRuntimeException("Request is not supported");
    }

    public boolean reply(CloudEvent cloudEvent, SendCallback sendCallback) {
        throw new ConnectorRuntimeException("Reply is not supported");
    }

    public void sendOneway(CloudEvent message) {
        sendAsync(message, NullSendCallback.instance());
    }

    public void sendAsync(CloudEvent cloudEvent, SendCallback sendCallback) {
        String topic = cloudEvent.getSubject();
        if (StrUtil.isBlank(topic)) {
            throw new BusinessException("Topic 不能为空");
        }
        try {
            this.producer.send(new ProducerRecord<>(topic, cloudEvent), (metadata, exception) -> {
                SendResult sendResult = new SendResult(cloudEvent.getId(), metadata.topic());
                if (null == exception) {
                    sendCallback.onSuccess(sendResult);
                } else {
                    sendCallback.onException(new ConnectorRuntimeException(exception), sendResult);
                }
                sendCallback.onCompleted(sendResult);
            });
        } catch (Exception e) {
            log.error(String.format("Send Async Exception, %s", cloudEvent), e);
            sendCallback.onException(new ConnectorRuntimeException(e), new SendResult(cloudEvent.getId(), topic));
        }
    }
}
