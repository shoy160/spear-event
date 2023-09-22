package cn.spear.event.business.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
public class BaseAuditPO extends BasePO {
    @TableField(fill = FieldFill.INSERT)
    private String creatorId;

    @TableField(fill = FieldFill.UPDATE)
    private String modifierId;
}
