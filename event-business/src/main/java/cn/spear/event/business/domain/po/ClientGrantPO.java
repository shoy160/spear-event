package cn.spear.event.business.domain.po;

import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.OperationTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.business.domain.enums.StatusEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Getter
@Setter
@TableName("client_grant")
public class ClientGrantPO extends BasePO {
    @TableId
    private Long id;
    private Long clientId;
    /**
     * 授权类型
     */
    private GrantTypeEnum type;

    private OperationTypeEnum operation;
    /**
     * 授权模式
     */
    private ResourcePatternEnum patternType;
    /**
     * 编码
     */
    private String topic;
    private String source;
    private String group;
    private String host;
    private StatusEnum status;
}
