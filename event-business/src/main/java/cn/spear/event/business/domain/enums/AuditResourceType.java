package cn.spear.event.business.domain.enums;

import cn.spear.event.core.annotation.Naming;
import cn.spear.event.core.enums.BaseNamedEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Naming(name = "auditResource")
public enum AuditResourceType implements BaseNamedEnum {

    /**
     * 审计资源
     */
    App("应用", 1),
    Event("事件", 2),
    Client("客户端", 3),
    Grant("授权关系", 4),
    Group("分组标签", 5);


    @EnumValue
    private final int value;
    private final String name;

    AuditResourceType(String name, int value) {
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
