package cn.spear.event.business.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author leizeyu
 * @date 2023/2/24
 */
@Getter
@Setter
@ToString
@TableName("event_schema")
public class EventSchemaPO extends BasePO {
    @TableId
    private Long id;

    private String code;

    private String schema;

}
