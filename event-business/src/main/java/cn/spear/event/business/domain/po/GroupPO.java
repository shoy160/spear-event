package cn.spear.event.business.domain.po;

import cn.spear.event.business.domain.enums.GroupTypeEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 事件分组实体
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
@TableName(value = "group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPO extends BasePO {
    @TableId
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
}
