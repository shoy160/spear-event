package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Optional;

/**
 * @author luoyong
 * @date 2023/2/16
 */
@Getter
@Setter
@ToString
public class GroupDTO extends BaseDTO {

    private Long id;

    /**
     * 分组类型
     */
    private GroupTypeEnum type;

    /**
     * 应用标识
     */
    private String appCode;

    /**
     * 父级 ID
     */
    private Long parentId;

    /**
     * 名称
     */
    private String name;

    /**
     * logo
     */
    private String logo;

    /**
     * 排序
     */
    private Integer sort;

    private Date createdAt;

    public Integer getSort() {
        return Optional.ofNullable(this.sort).orElse(0);
    }
}
