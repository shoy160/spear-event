package cn.spear.event.business.domain.enums;

import cn.spear.event.core.annotation.Naming;
import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Naming(name = "auditOperation")
public enum AuditOperationType implements BaseNamedEnum {
    /**
     * 审计资源
     */
    Add("新增", 1),
    Delete("删除", 2),
    Edit("编辑", 3),
    Update("状态更新", 4);


    @EnumValue
    private final int value;
    private final String name;

    AuditOperationType(String name, int value) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
