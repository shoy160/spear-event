package cn.spear.event.business.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * @author leizeyu
 * @date 2023/3/6
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("config")
public class ConfigPO extends BasePO {

    @TableId
    private Long id;

    private String key;

    private String value;
    private String valueType;

    private String profile;
}
