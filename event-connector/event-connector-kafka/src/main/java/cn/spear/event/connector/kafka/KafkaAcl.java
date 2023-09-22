package cn.spear.event.connector.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.connector.kafka.config.KafkaConnectorProperties;
import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.ConnectorAcl;
import cn.spear.event.core.connector.enums.*;
import cn.spear.event.core.connector.model.*;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.domain.dto.StringEntry;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.exception.ConnectorRuntimeException;
import cn.spear.event.core.utils.EnumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.scram.internals.ScramFormatter;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAcl implements ConnectorAcl {
    private static final int DEFAULT_SCRAM_ITERATIONS = 4096;
    private static final int ADMIN_CLIENT_TIMEOUT = 2000;
    private static final String PRINCIPAL_PREFIX = "User:";
    private final KafkaConnectorProperties config;
    private final AdminClient adminClient;

    private UserScramCredentialsDescription userDescribe(String username) {
        DescribeUserScramCredentialsOptions options = new DescribeUserScramCredentialsOptions()
                .timeoutMs(ADMIN_CLIENT_TIMEOUT);
        try {
            Map<String, UserScramCredentialsDescription> descriptionMap = adminClient
                    .describeUserScramCredentials(Collections.singletonList(username), options)
                    .all().get();
            return descriptionMap.get(username);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取 Kafka 用户描述异常", e);
        }
        return null;
    }

    @Override
    public List<String> userList() {
        DescribeUserScramCredentialsOptions options = new DescribeUserScramCredentialsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        try {
            Map<String, UserScramCredentialsDescription> descriptionMap =
                    adminClient.describeUserScramCredentials(null, options)
                            .all().get();
            return descriptionMap
                    .values()
                    .stream()
                    .map(UserScramCredentialsDescription::name)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取 Kafka 用户列表异常", e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean createUser(String username, String password) {
        if (!config.enableAcl) {
            return true;
        }
        UserScramCredentialsDescription userDescribe = userDescribe(username);
        if (Objects.nonNull(userDescribe) && CollUtil.isNotEmpty(userDescribe.credentialInfos())) {
            return false;
        }
        return updateUser(username, password);
    }

    @Override
    public boolean deleteUser(String username) {
        if (!config.enableAcl) {
            return true;
        }
        AlterUserScramCredentialsResult alterResult = adminClient.alterUserScramCredentials(
                Arrays.asList(
                        // scram-256
                        new UserScramCredentialDeletion(
                                username,
                                ScramMechanism.SCRAM_SHA_256),
                        // scram-512
                        new UserScramCredentialDeletion(
                                username,
                                ScramMechanism.SCRAM_SHA_512)
                ),
                new AlterUserScramCredentialsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT)
        );
        try {
            alterResult.all().get();
            log.info("删除 Kafka 用户[{}]成功", username);
        } catch (Exception ex) {
            log.error("删除 Kafka 用户失败", ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateUser(String username, String password) {
        if (!config.enableAcl) {
            return true;
        }
        AlterUserScramCredentialsResult alterResult = adminClient.alterUserScramCredentials(
                Arrays.asList(
                        // scram-256
                        new UserScramCredentialUpsertion(
                                username,
                                new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_256, DEFAULT_SCRAM_ITERATIONS),
                                password.getBytes(StandardCharsets.UTF_8),
                                ScramFormatter.secureRandomBytes(new SecureRandom())),
                        // scram-512
                        new UserScramCredentialUpsertion(
                                username,
                                new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_512, DEFAULT_SCRAM_ITERATIONS),
                                password.getBytes(StandardCharsets.UTF_8),
                                ScramFormatter.secureRandomBytes(new SecureRandom()))
                ),
                new AlterUserScramCredentialsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT)
        );
        try {
            alterResult.all().get();
        } catch (Exception ex) {
            log.error("更新 Kafka 用户凭证失败", ex);
            return false;
        }
        return true;
    }

    @Override
    public int createTopic(
            String topicName, Integer partitions, Integer replications, Integer retention
    ) {
        partitions = partitions == null ? 1 : partitions;
        if (partitions > this.config.getMaxPartitions()) {
            throw new ConnectorRuntimeException(String.format("分片数不能大于最大分片数:%d", this.config.getMaxPartitions()));
        }
        TopicDescription description = topicDescription(topicName);
        if (null != description) {
            // Topic 已存在
            if (null == description.partitions()) {
                throw new ConnectorRuntimeException(String.format("Kafka Topic[%s]异常", topicName));
            }
            int currentPartitions = description.partitions().size();
            if (partitions > currentPartitions) {
                //分片扩容
                extendPartitions(topicName, partitions);
            }
            updateTopic(topicName, retention);
            return description.partitions().get(0).replicas().size();
        }

        int nodeCount = getNodeCount();
        if (Objects.isNull(replications) || replications > nodeCount) {
            replications = nodeCount;
        }
        NewTopic topic = new NewTopic(topicName, partitions, replications.shortValue());
        long param = retention * 24 * 60 * 60 * 1000L;
        Map<String, String> configs = new HashMap<>(1);
        configs.put("retention.ms", String.valueOf(param));
        topic.configs(configs);
        CreateTopicsResult createTopicsResult =
                adminClient.createTopics(Collections.singletonList(topic), new CreateTopicsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT));
        try {
            createTopicsResult.all().get();
        } catch (Exception ex) {
            log.error("创建 topic 参数异常", ex);
            throw new ConnectorRuntimeException(String.format("创建 topic 参数异常，返回信息：%s", ex.getMessage()));
        }
        return replications;
    }

    @Override
    public boolean existsTopic(String topicName) {
        return null != topicDescription(topicName);
    }

    @Override
    public boolean updateTopic(String topicName, Integer retention) {
        Map<ConfigResource, Collection<AlterConfigOp>> alertConfigs = new HashMap<>();
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        Collection<AlterConfigOp> configOps = new ArrayList<>();
        if (null != retention && retention > 0) {
            //转换为毫秒
            long retentionMs = retention * 24 * 60 * 60 * 1000L;
            configOps.add(new AlterConfigOp(
                    new ConfigEntry("retention.ms", String.valueOf(retentionMs)),
                    AlterConfigOp.OpType.SET
            ));
        }
        if (CollUtil.isEmpty(configOps)) {
            return false;
        }
        alertConfigs.put(configResource, configOps);
        AlterConfigsOptions options = new AlterConfigsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        AlterConfigsResult alterConfigsResult =
                adminClient.incrementalAlterConfigs(alertConfigs, options);
        try {
            alterConfigsResult.all().get();
        } catch (Exception exc) {
            log.warn(String.format("修改 topic[%s] 参数异常，返回信息：%s", topicName, exc.getMessage()), exc);
            return false;
        }
        return true;
    }

    @Override
    public boolean extendPartitions(String topicName, Integer partitions) {
        if (null == partitions || partitions < 2) {
            throw new ConnectorRuntimeException("扩展分区时，分区数不能小于 2");
        }
        Map<String, NewPartitions> newPartitions = new HashMap<>(1);
        newPartitions.put(topicName, NewPartitions.increaseTo(partitions));
        CreatePartitionsResult partitionsResult = adminClient
                .createPartitions(newPartitions, new CreatePartitionsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT));
        try {
            partitionsResult.all().get();
        } catch (InterruptedException | ExecutionException e) {
            String message = String.format("扩展 topic[%s] 分区异常，返回信息：%s", topicName, e.getMessage());
            log.warn(message, e);
            throw new ConnectorRuntimeException(message);
        }
        return true;
    }

    @Override
    public boolean deleteTopic(String topicName) {
        Set<String> topics = getTopicList();
        if (!topics.contains(topicName)) {
            log.info("topic 不存在，名称：{}", topicName);
            return true;
        }
        DeleteTopicsResult deleteTopicsResult =
                adminClient.deleteTopics(Collections.singletonList(topicName), new DeleteTopicsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT));
        try {
            deleteTopicsResult.all().get();
        } catch (Exception exc) {
            log.warn("删除 topic 参数异常", exc);
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getTopicList() {
        Set<String> result = null;
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        try {
            result = listTopicsResult.names().get();
        } catch (Exception e) {
            log.warn("获取主题列表失败", e);
        }
        return result;
    }

    @Override
    public List<EventAclBinding> getAclList() {
        DescribeAclsOptions options = new DescribeAclsOptions();
        options.timeoutMs(ADMIN_CLIENT_TIMEOUT);
        DescribeAclsResult result = adminClient.describeAcls(AclBindingFilter.ANY, options);
        try {
            Collection<AclBinding> bindings = result.values().get();
            return bindings.stream().map(t -> {
                AccessControlEntry entry = t.entry();
                ResourcePattern pattern = t.pattern();
                EventAclBinding binding = new EventAclBinding();
                binding.setHost(entry.host());
                binding.setUser(entry.principal().replace(PRINCIPAL_PREFIX, Constants.STR_EMPTY));
                EventAclOperation operation = EnumUtils.convertByName(entry.operation(), EventAclOperation.class);
                binding.setOperation(operation);
                binding.setGrantType(EventAclOperation.READ.equals(operation) ? EventGrantType.Consumer : EventGrantType.Producer);
                EventPatternType patternType = EnumUtils.convertByName(pattern.patternType(), EventPatternType.class);
                binding.setPatternType(patternType);
                switch (pattern.resourceType()) {
                    case TOPIC:
                        binding.setTopic(pattern.name());
                        break;
                    case GROUP:
                        binding.setGroup(pattern.name());
                        break;
                    default:
                        break;
                }
                return binding;
            }).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    @Override
    public PagedDTO<EventRecord> getTopicRecords(int size, String topic, Integer partition, ConsumerResetType resetType) {
        KafkaConsumer<String, String> kafkaConsumer = null;
        final Duration timeout = Duration.ofMillis(2000L);
        try {
            Map<TopicPartition, Long> beginOffsets = getPartitionsOffset(topic, partition, OffsetSpec.earliest());
            Map<TopicPartition, Long> endOffsets = getPartitionsOffset(topic, partition, OffsetSpec.latest());
            if (MapUtil.isEmpty(beginOffsets) || MapUtil.isEmpty(endOffsets)) {
                return PagedDTO.paged(new ArrayList<>(0));
            }
            List<TopicPartition> partitionList = new ArrayList<>();
            long total = 0;
            for (Map.Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
                long begin = beginOffsets.get(entry.getKey());
                long end = entry.getValue();
                if (begin == end) {
                    continue;
                }
                total += end - begin;
                partitionList.add(entry.getKey());
            }
            if (CollUtil.isEmpty(partitionList)) {
                return PagedDTO.paged(new ArrayList<>(0));
            }
            long maxMessage = Math.min(total, size);
            kafkaConsumer = createConsumer(size);
            kafkaConsumer.assign(partitionList);

            for (TopicPartition topicPartition : partitionList) {
                if (ConsumerResetType.Earliest == resetType) {
                    // 重置到最旧
                    kafkaConsumer.seek(topicPartition, beginOffsets.get(topicPartition));
                } else {
                    // 默认，重置到最新
                    kafkaConsumer.seek(topicPartition, Math.max(beginOffsets.get(topicPartition), endOffsets.get(topicPartition) - size));
                }
            }

            List<EventRecord> recordList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            do {
                if (System.currentTimeMillis() - startTime > 6000L) {
                    break;
                }
                ConsumerRecords<String, String> records = kafkaConsumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    EventRecord eventRecord = new EventRecord();
                    eventRecord.setTopic(record.topic());
                    eventRecord.setPartition(record.partition());
                    eventRecord.setTimestamp(record.timestamp());
                    eventRecord.setOffset(record.offset());
                    eventRecord.setKey(record.key());
                    eventRecord.setValue(record.value());
                    List<StringEntry> headers = new ArrayList<>();
                    for (Header header : record.headers()) {
                        headers.add(StringEntry.of(header.key(), new String(header.value(), StandardCharsets.UTF_8)));
                    }
                    eventRecord.setHeaders(headers);
                    recordList.add(eventRecord);
                }
            } while (maxMessage > recordList.size());
            Comparator<EventRecord> comparator = Comparator.comparing(EventRecord::getTimestamp);
            comparator = comparator.thenComparing(EventRecord::getOffset).reversed();
            recordList.sort(comparator);
            return PagedDTO.paged(recordList.subList(0, (int) maxMessage), total, 1, size);
        } finally {
            if (Objects.nonNull(kafkaConsumer)) {
                kafkaConsumer.close(timeout);
            }
        }
    }

    @Override
    public Map<String, Long> getMessageCounts(Collection<String> topics) {
        try {
            Map<String, TopicDescription> descriptionMap = adminClient.describeTopics(topics)
                    .allTopicNames().get();
            List<TopicPartition> topicPartitions = descriptionMap.values().stream()
                    .flatMap(t -> t.partitions().stream().map(p -> new TopicPartition(t.name(), p.partition())))
                    .collect(Collectors.toList());
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> beginOffsetMap = adminClient.listOffsets(topicPartitions.stream().collect(Collectors.toMap(k -> k, v -> OffsetSpec.earliest())))
                    .all().get();
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsetMap = adminClient.listOffsets(topicPartitions.stream().collect(Collectors.toMap(k -> k, v -> OffsetSpec.latest())))
                    .all().get();
            Map<String, Long> countMap = new HashMap<>();
            for (Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> entry : endOffsetMap.entrySet()) {
                TopicPartition key = entry.getKey();
                ListOffsetsResult.ListOffsetsResultInfo beginOffset = beginOffsetMap.get(key);
                if (Objects.isNull(beginOffset)) {
                    continue;
                }
                long count = entry.getValue().offset() - beginOffset.offset();
                if (countMap.containsKey(key.topic())) {
                    count += countMap.get(key.topic());
                }
                countMap.put(key.topic(), count);
            }
            return countMap;
        } catch (InterruptedException | ExecutionException ex) {
            log.warn("查询 Topic 消息数异常", ex);
        }
        return new HashMap<>(0);
    }

    @Override
    public void addAcl(EventAclBinding aclBinding) {
        if (!config.enableAcl) {
            return;
        }
        List<AclBinding> aclBindings = createAclBindings(aclBinding);
        CreateAclsOptions options = new CreateAclsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        CreateAclsResult createAclsResult = adminClient.createAcls(aclBindings, options);

        try {
            createAclsResult.all().get();
            log.info("创建授权成功-> {}", aclBinding);
        } catch (Exception exc) {
            log.warn("创建权限失败，错误信息：{}", exc.getMessage());
            throw new ConnectorRuntimeException("创建 ACL 权限失败");
        }
    }

    @Override
    public void deleteAcl(EventAclBinding aclBinding) {
        batchDeleteAcl(Collections.singletonList(aclBinding));
    }

    @Override
    public void batchDeleteAcl(Collection<EventAclBinding> bindings) {
        if (!config.enableAcl) {
            return;
        }
        List<AclBinding> aclBindings = new ArrayList<>();
        for (EventAclBinding binding : bindings) {
            aclBindings.addAll(createAclBindings(binding));
        }
        DeleteAclsOptions options = new DeleteAclsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        List<AclBindingFilter> filters = aclBindings.stream()
                .map(AclBinding::toFilter)
                .collect(Collectors.toList());
        DeleteAclsResult deleteAclsResult = adminClient.deleteAcls(filters, options);
        try {
            deleteAclsResult.all().get();
            log.info("删除 ACL 权限成功");
        } catch (Exception exc) {
            log.warn("删除权限失败，错误信息：{}", exc.getMessage());
            throw new ConnectorRuntimeException("删除 ACL 权限失败");
        }
    }

    @Override
    public PagedDTO<EventConsumer> getConsumers(int page, int size, String groupId, String topicName, ConsumerState state, Boolean isSimple) {
        ListConsumerGroupsOptions groupsOptions =
                new ListConsumerGroupsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        try {
            Collection<ConsumerGroupListing> groupListings =
                    adminClient.listConsumerGroups(groupsOptions)
                            .all().get();
            List<String> groups = groupListings.stream()
                    .filter(t -> {
                        if (StrUtil.isNotBlank(groupId) && !t.groupId().contains(groupId)) {
                            return false;
                        }
                        if (Objects.nonNull(state)) {
                            ConsumerState consumerState = convertState(t.state().orElse(ConsumerGroupState.UNKNOWN));
                            if (!state.equals(consumerState)) {
                                return false;
                            }
                        }
                        return Objects.isNull(isSimple) || t.isSimpleConsumerGroup() == isSimple;
                    })
                    .map(ConsumerGroupListing::groupId)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(groups)) {
                return PagedDTO.paged(new ArrayList<>(0), 0, page, size);
            }
            if (StrUtil.isBlank(topicName)) {
                List<EventConsumer> consumers = CollUtil.page(page - 1, size, groups)
                        .stream()
                        .map(this::getConsumer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return PagedDTO.paged(consumers, groups.size(), page, size);
            } else {
                List<EventConsumer> eventConsumers = groups.stream()
                        .map(this::getConsumer)
                        .filter(t -> {
                            if (Objects.isNull(t)) {
                                return false;
                            }
                            return t.getTopicMembers().stream().anyMatch(m -> topicName.equals(m.getTopicName()));
                        })
                        .collect(Collectors.toList());
                return PagedDTO.paged(CollUtil.page(page - 1, size, eventConsumers), eventConsumers.size(), page, size);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取 Kafka 消费组信息异常", e);
        }
        return PagedDTO.paged(new ArrayList<>(0), 0, page, size);
    }

    @Override
    public List<EventTopicMemberDetail> getConsumerDetails(String groupId, String topicName) {
        ConsumerGroupDescription description = getConsumerDescription(groupId);
        if (Objects.isNull(description)) {
            return new ArrayList<>(0);
        }
        // 转换存储格式
        Map<TopicPartition, MemberDescription> memberDescriptionMap = new HashMap<>();
        for (MemberDescription memberDescription : description.members()) {
            for (TopicPartition tp : memberDescription.assignment().topicPartitions()) {
                memberDescriptionMap.put(tp, memberDescription);
            }
        }
        Map<TopicPartition, Long> consumerOffset = getConsumerOffset(groupId);
        Map<TopicPartition, Long> partitionsOffset = getPartitionsOffset(topicName, null, OffsetSpec.latest());
        List<EventTopicMemberDetail> details = new ArrayList<>();
        for (Map.Entry<TopicPartition, Long> entry : consumerOffset.entrySet()) {
            TopicPartition topicPartition = entry.getKey();
            if (!Objects.equals(topicPartition.topic(), topicName)) {
                continue;
            }
            EventTopicMemberDetail detail = new EventTopicMemberDetail();
            detail.setTopicName(topicName);
            detail.setPartitionId(topicPartition.partition());
            MemberDescription memberDescription = memberDescriptionMap.get(topicPartition);
            if (Objects.nonNull(memberDescription)) {
                detail.setMemberId(memberDescription.consumerId());
                detail.setClientId(memberDescription.clientId());
                detail.setHost(memberDescription.host());
            }
            Long logEndOffset = partitionsOffset.get(topicPartition);
            Long currentOffset = entry.getValue();
            detail.setLogEndOffset(logEndOffset);
            detail.setCurrentOffset(currentOffset);
            detail.setLag(Math.max(0, logEndOffset - currentOffset));
            details.add(detail);
        }
        details.sort(Comparator.comparing(EventTopicMemberDetail::getPartitionId));
        return details;
    }

    @Override
    public void resetConsumer(String groupId, Map<EventPartition, Long> offsets) {
        AlterConsumerGroupOffsetsOptions options =
                new AlterConsumerGroupOffsetsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        try {
            Map<TopicPartition, OffsetAndMetadata> offsetMap = offsets.keySet()
                    .stream()
                    .collect(Collectors.toMap(k ->
                                    new TopicPartition(k.getTopic(), k.getPartition()),
                            v -> new OffsetAndMetadata(offsets.get(v))));
            adminClient.alterConsumerGroupOffsets(groupId, offsetMap, options)
                    .all().get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("重置 Kafka 消费组 Offset 异常", e);
            throw new BusinessException("重置消费组 Offset 异常");
        }
    }

    private EventConsumer getConsumer(String groupId) {
        ConsumerGroupDescription description = getConsumerDescription(groupId);
        if (Objects.isNull(description)) {
            return null;
        }
        EventConsumer consumer = new EventConsumer();
        consumer.setGroupId(groupId);
        consumer.setSimple(description.isSimpleConsumerGroup());
        consumer.setState(convertState(description.state()));
        consumer.setMemberCount(Objects.isNull(description.members()) ? 0 : description.members().size());
        consumer.setPartitionAssignor(description.partitionAssignor());
        consumer.setCoordinatorId(Objects.isNull(description.coordinator()) ? -1 : description.coordinator().id());
        consumer.setTopicMembers(new ArrayList<>());

        // 获取消费组消费过哪些Topic
        Map<String, EventTopicMember> memberMap = new HashMap<>();
        for (TopicPartition tp : getConsumerOffset(groupId).keySet()) {
            memberMap.putIfAbsent(tp.topic(), new EventTopicMember(tp.topic(), 0));
        }

        // 记录成员信息
        for (MemberDescription memberDescription : description.members()) {
            Set<TopicPartition> partitionList = new HashSet<>();
            if (Objects.nonNull(memberDescription.assignment().topicPartitions())) {
                partitionList = memberDescription.assignment().topicPartitions();
            }

            Set<String> topicNameSet = partitionList.stream()
                    .map(TopicPartition::topic).collect(Collectors.toSet());
            for (String topicName : topicNameSet) {
                memberMap.putIfAbsent(topicName, new EventTopicMember(topicName, 0));

                EventTopicMember member = memberMap.get(topicName);
                member.setMemberCount(member.getMemberCount() + 1);
            }
        }
        consumer.setTopicMembers(new ArrayList<>(memberMap.values()));

        return consumer;
    }

    private ConsumerState convertState(ConsumerGroupState state) {
        switch (state) {
            case PREPARING_REBALANCE:
            case COMPLETING_REBALANCE:
                return ConsumerState.REBALANCE;
            case STABLE:
                return ConsumerState.ACTIVE;
            case EMPTY:
                return ConsumerState.EMPTY;
            case DEAD:
                return ConsumerState.DEAD;
            default:
                return ConsumerState.UNKNOWN;
        }
    }

    private Map<TopicPartition, Long> getConsumerOffset(String groupId) {
        ListConsumerGroupOffsetsOptions offsetsOptions =
                new ListConsumerGroupOffsetsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        Map<TopicPartition, Long> offsetMap = new HashMap<>();
        try {
            Map<TopicPartition, OffsetAndMetadata> metadataMap =
                    adminClient.listConsumerGroupOffsets(groupId, offsetsOptions)
                            .partitionsToOffsetAndMetadata().get();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : metadataMap.entrySet()) {
                offsetMap.put(entry.getKey(), entry.getValue().offset());
            }
            return offsetMap;
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取 Kafka 消费组 Offsets 异常", e);
        }
        return new HashMap<>(0);
    }

    private ConsumerGroupDescription getConsumerDescription(String groupId) {
        DescribeConsumerGroupsOptions options = new DescribeConsumerGroupsOptions()
                .timeoutMs(ADMIN_CLIENT_TIMEOUT).includeAuthorizedOperations(false);
        try {
            Map<String, ConsumerGroupDescription> descriptionMap =
                    adminClient.describeConsumerGroups(Collections.singletonList(groupId), options)
                            .all().get();
            return descriptionMap.get(groupId);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取 Kafka 订阅组信息异常", e);
        }
        return null;
    }

    private Map<TopicPartition, Long> getPartitionsOffset(String topic, Integer partition, OffsetSpec offsetSpec) {
        TopicDescription topicDescription = topicDescription(topic);
        if (Objects.isNull(topicDescription)) {
            return new HashMap<>(0);
        }
        Map<TopicPartition, OffsetSpec> offsetSpecMap = topicDescription
                .partitions()
                .stream()
                .filter(t -> {
                    if (null == partition) {
                        return true;
                    }
                    return Objects.equals(t.partition(), partition);
                })
                .map(t -> new TopicPartition(topic, t.partition()))
                .collect(Collectors.toMap(k -> k, v -> offsetSpec));

        ListOffsetsResult offsets = adminClient
                .listOffsets(offsetSpecMap, new ListOffsetsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT));
        try {
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> infoMap = offsets.all().get();
            return infoMap.keySet()
                    .stream()
                    .collect(Collectors.toMap(k -> k, v -> infoMap.get(v).offset()));
        } catch (InterruptedException | ExecutionException ex) {
            log.warn("获取 Offset 异常", ex);
        }
        return new HashMap<>(0);
    }

    private TopicDescription topicDescription(String topicName) {
        Map<String, TopicDescription> descriptionMap = topicDescription(Collections.singletonList(topicName));
        return MapUtil.isEmpty(descriptionMap) ? null : descriptionMap.get(topicName);
    }

    private Map<String, TopicDescription> topicDescription(Collection<String> topicNames) {
        DescribeTopicsOptions options = new DescribeTopicsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT);
        DescribeTopicsResult describeResult = adminClient.describeTopics(topicNames, options);
        try {
            return describeResult.allTopicNames().get();
        } catch (Exception e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.debug("Topic[{}]不存在", String.join(",", topicNames));
            } else {
                log.warn(String.format("获取Topic[%s]信息失败", String.join(",", topicNames)), e);
            }
        }
        return null;
    }

    private List<AclBinding> createAclBindings(EventAclBinding aclBinding) {
        if (null == aclBinding) {
            throw new ConnectorRuntimeException("绑定 ACL 权限失败");
        }
        PatternType patternType =
                EnumUtils.convertByName(aclBinding.getPatternType(), PatternType.class, PatternType.LITERAL);
        List<AclBinding> aclBindings = new ArrayList<>();
        if (EnumUtils.hasFlag(EventGrantType.Producer, aclBinding.getGrantType())) {
            if (StrUtil.isBlank(aclBinding.getTopic())) {
                throw new ConnectorRuntimeException("绑定 ACL 权限失败，Topic 主题异常");
            }
            ResourcePattern resource = new ResourcePattern(ResourceType.TOPIC, aclBinding.getTopic(), patternType);
            AccessControlEntry accessControlEntry =
                    new AccessControlEntry(PRINCIPAL_PREFIX + aclBinding.getUser(), aclBinding.getHost(), AclOperation.WRITE, AclPermissionType.ALLOW);
            aclBindings.add(new AclBinding(resource, accessControlEntry));
        }
        if (EnumUtils.hasFlag(EventGrantType.Consumer, aclBinding.getGrantType())) {
            AccessControlEntry accessControlEntry =
                    new AccessControlEntry(PRINCIPAL_PREFIX + aclBinding.getUser(), aclBinding.getHost(), AclOperation.READ, AclPermissionType.ALLOW);
            if (StrUtil.isNotBlank(aclBinding.getTopic())) {
                ResourcePattern resource = new ResourcePattern(ResourceType.TOPIC, aclBinding.getTopic(), patternType);
                aclBindings.add(new AclBinding(resource, accessControlEntry));
            }
            if (StrUtil.isNotBlank(aclBinding.getGroup())) {
                // 分组授权
                ResourcePattern groupResource = new ResourcePattern(ResourceType.GROUP, aclBinding.getGroup(), PatternType.LITERAL);
                aclBindings.add(new AclBinding(groupResource, accessControlEntry));
            }
        }
        return aclBindings;
    }

    private int getNodeCount() {
        try {
            Collection<Node> nodes = adminClient.describeCluster().nodes().get();
            return nodes.size();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("获取节点数异常", e);
            return config.getReplications();
        }
    }

    private KafkaConsumer<String, String> createConsumer(int maxPollSize) {
        Properties properties = new Properties();
        config.createConsumer(properties);
        config.createSaslJaasConfig(properties, config.getUsername(), config.getPassword());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollSize);
        return new KafkaConsumer<>(properties);
    }
}
