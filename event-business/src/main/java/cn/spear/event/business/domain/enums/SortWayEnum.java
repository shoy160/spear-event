package cn.spear.event.business.domain.enums;

import cn.spear.event.core.annotation.Naming;
import cn.spear.event.core.enums.BaseNamedEnum;

/**
 * @author leizeyu
 * @date 2023/3/2
 */
@Naming(name = "sortWay")
public enum SortWayEnum implements BaseNamedEnum {

    /**
     * 排序方式
     */
    ascend("升序", 1),
    descend("降序", 2);

    private final String name;
    private final int value;

    SortWayEnum(String name, int value) {
        this.name = name;
        this.value = value;
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
