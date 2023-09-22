/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.spear.event.connector.kafka.consumer;

import cn.spear.event.core.connector.EventListener;
import cn.spear.event.core.connector.context.AsyncConsumeContext;
import cn.spear.event.core.enums.EventAction;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
public class KafkaConsumerRunner implements Runnable {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final KafkaConsumer<String, CloudEvent> consumer;
    private final ConcurrentHashMap<String, Long> cloudEventToOffset;
    private EventListener listener;

    public KafkaConsumerRunner(KafkaConsumer<String, CloudEvent> kafkaConsumer) {
        this.consumer = kafkaConsumer;
        cloudEventToOffset = new ConcurrentHashMap<>();
    }

    public synchronized void setListener(EventListener listener) {
        this.listener = listener;
    }

    public long getOffset(CloudEvent cloudEvent) {
        return cloudEventToOffset.getOrDefault(cloudEvent.getId(), 0L);
    }

    @Override
    public void run() {
        while (!closed.get()) {
            try {
                ConsumerRecords<String, CloudEvent> records = consumer.poll(Duration.ofMillis(2000));
                if (records.isEmpty() || null == listener) {
                    continue;
                }
                if (listener.enableBatch()) {
                    batchConsume(records);
                } else {
                    singleConsume(records);
                }
            } catch (WakeupException e) {
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                cloudEventToOffset.clear();
            }
        }
    }

    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }

    private void singleConsume(ConsumerRecords<String, CloudEvent> records) {
        // Handle new records
        final List<ConsumerRecord<String, CloudEvent>> commitRecords = new ArrayList<>();
        records.forEach(rec -> {
            try {
                CloudEvent cloudEvent = rec.value();
                final long offset = rec.offset();
                final int partition = rec.partition();
                String topicName = cloudEvent.getSubject();
                AsyncConsumeContext eventMeshAsyncConsumeContext = new AsyncConsumeContext() {
                    @Override
                    public void commit(EventAction action) {
                        switch (action) {
                            case CommitMessage:
                                // update offset
                                log.info("message commit, topic: {}, current offset:{},{}", topicName, partition, offset);
                                commitRecords.add(rec);
                                break;
                            case ReconsumeLater:
                                // don't update offset
                                break;
                            case ManualAck:
                                // update offset
                                log.info("message ack, topic: {}, current offset:{},{}", topicName, partition, offset);
                                commitRecords.add(rec);
                                break;
                            default:
                        }
                    }
                };
                cloudEventToOffset.put(cloudEvent.getId(), offset);
                if (listener != null) {
                    listener.consume(cloudEvent, eventMeshAsyncConsumeContext);
                }
            } catch (Exception e) {
                log.info("Error parsing cloudevent: {}", e.getMessage());
            }
        });
        batchCommitSync(commitRecords);
    }

    private void batchConsume(ConsumerRecords<String, CloudEvent> records) {
        List<CloudEvent> eventList = new ArrayList<>(records.count());
        for (ConsumerRecord<String, CloudEvent> record : records) {
            CloudEvent event = record.value();
            cloudEventToOffset.put(event.getId(), record.offset());
            eventList.add(event);
        }
        listener.batchConsume(eventList, new AsyncConsumeContext() {
            @Override
            public void commit(EventAction action) {
                switch (action) {
                    case CommitMessage:
                    case ManualAck:
                        consumer.commitSync();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void commitSync(ConsumerRecord<String, CloudEvent> record) {
        batchCommitSync(Collections.singletonList(record));
    }

    private void batchCommitSync(List<ConsumerRecord<String, CloudEvent>> records) {
        Map<TopicPartition, OffsetAndMetadata> commitMap =
                records.stream()
                        .collect(Collectors.toMap(k -> new TopicPartition(k.topic(), k.partition()), v -> new OffsetAndMetadata(v.offset())));
        this.consumer.commitSync(commitMap);
    }
}



