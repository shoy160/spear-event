package cn.spear.event.business.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author luoyong
 * @date 2022/12/9
 */
@Getter
@Setter
@TableName(value = "event_data")
public class EventDataPO extends BasePO {
    @TableId
    private String id;
    private String topic;
    private byte[] data;
    private Date sendAt;
}
