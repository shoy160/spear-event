package cn.spear.event.business.domain.po;

import cn.spear.event.core.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
public class BasePO implements BaseEntity {
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Version
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;

    @TableField
    @TableLogic
    private boolean isDeleted;

    @Override
    public Date getCreatedTime() {
        return this.createdAt;
    }

    @Override
    public boolean isDeleted() {
        return this.isDeleted;
    }
}
