package cn.spear.event.core.connector;

import cn.spear.event.core.connector.enums.ConsumerResetType;
import cn.spear.event.core.connector.enums.ConsumerState;
import cn.spear.event.core.connector.model.*;
import cn.spear.event.core.domain.dto.PagedDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author luoyong
 * @date 2022/11/9
 */
public interface ConnectorAcl {

    /**
     * 获取用户列表
     *
     * @return
     */
    List<String> userList();

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param password 用户密码
     * @return boolean
     */
    boolean createUser(String username, String password);

    /**
     * 删除用户
     *
     * @param username 用户名
     * @return boolean
     */
    boolean deleteUser(String username);

    /**
     * 更新用户
     *
     * @param username 用户名
     * @param password 用户密码
     * @return
     */
    boolean updateUser(String username, String password);

    /**
     * 创建 Topic
     *
     * @param topicName    主题名称
     * @param partitions   分区数
     * @param replications 副本数
     * @param retention    数据有效期(天)
     * @return 副本数
     */
    int createTopic(String topicName, Integer partitions, Integer replications, Integer retention);

    /**
     * 是否存在主题
     *
     * @param topicName 主题名称
     * @return boolean
     */
    boolean existsTopic(String topicName);

    /**
     * 修改 Topic 有效期
     *
     * @param topicName 主题名称
     * @param retention 数据有效期(天)
     * @return boolean
     */
    boolean updateTopic(String topicName, Integer retention);

    /**
     * 扩展分区
     *
     * @param topicName  主题名称
     * @param partitions 分区数（只能多不能少）
     * @return boolean
     */
    boolean extendPartitions(String topicName, Integer partitions);

    /**
     * 删除 Topic
     *
     * @param topicName 主题名称
     * @return boolean
     */
    boolean deleteTopic(String topicName);

    /**
     * 主题列表
     *
     * @return Set of Topics
     */
    Set<String> getTopicList();

    /**
     * 添加 ACL 授权
     *
     * @param binding 授权实体
     * @return boolean
     */
    void addAcl(EventAclBinding binding);

    /**
     * 删除 ACL 授权
     *
     * @param binding 授权实体
     */
    void deleteAcl(EventAclBinding binding);

    /**
     * 删除 ACL 授权
     *
     * @param bindings 授权实体列表
     */
    void batchDeleteAcl(Collection<EventAclBinding> bindings);

    /**
     * 授权列表
     *
     * @return list of ACL
     */
    List<EventAclBinding> getAclList();

    /**
     * 获取主题记录值
     *
     * @param size      数量
     * @param topic     主题
     * @param partition 分区
     * @param resetType reset type
     * @return paged
     */
    PagedDTO<EventRecord> getTopicRecords(int size, String topic, Integer partition, ConsumerResetType resetType);

    /**
     * 获取消息数
     *
     * @param topics 主题列表
     * @return map
     */
    Map<String, Long> getMessageCounts(Collection<String> topics);

    /**
     * 获取消费组
     *
     * @param page      Page
     * @param size      Size
     * @param groupId   消费组 ID
     * @param topicName 主题
     * @param state     消费组状态
     * @param isSimple  是否是简单消费组
     * @return list of eventConsumer
     */
    PagedDTO<EventConsumer> getConsumers(int page, int size, String groupId, String topicName, ConsumerState state, Boolean isSimple);

    /**
     * 获取订阅详情
     *
     * @param groupId   订阅组
     * @param topicName 主题
     * @return list of eventTopicMemberDetail
     */
    List<EventTopicMemberDetail> getConsumerDetails(String groupId, String topicName);

    /**
     * 重置消费组
     *
     * @param groupId 消费组 ID
     * @param offsets Offsets
     */
    void resetConsumer(String groupId, Map<EventPartition, Long> offsets);
}
