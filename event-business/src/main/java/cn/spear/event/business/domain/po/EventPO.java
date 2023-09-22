package cn.spear.event.business.domain.po;

import cn.spear.event.business.domain.enums.EventTypeEnum;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
@TableName(value = "event")
public class EventPO extends BaseAuditPO {
    @TableId
    private Long id;
    /**
     * 事件类型
     */
    private EventTypeEnum type;
    /**
     * 私有 ID
     */
    private String privateId;
    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 模块编码
     */
    private String moduleCode;
    /**
     * 事件编码
     */
    private String code;
    /**
     * 事件名称
     */
    private String name;
    /**
     * 事件描述
     */
    private String desc;
    /**
     * 事件标签
     */
    private String tags;
    /**
     * 事件主题
     */
    private String topic;
    /**
     * 分片数
     */
    private Integer partition;
    /**
     * 副本数
     */
    private Integer replications;
    /**
     * 数据有效期(天)
     */
    private Integer retention;

    /**
     * 消息数量
     */
    private Long messageCount;

    /**
     * 排序值
     */
    private Integer sort;
    /**
     * 配置项
     */
    private Integer options;


    public String getTopic() {
        return StrUtil.isBlank(topic) ? code : topic;
    }
}
