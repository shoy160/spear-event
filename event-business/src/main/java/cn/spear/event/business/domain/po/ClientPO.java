package cn.spear.event.business.domain.po;

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
@TableName("client")
public class ClientPO extends BasePO {
    @TableId
    private Long id;
    private String name;
    private String secret;
    private StatusEnum status;
}
