package cn.spear.event.business.domain.po;

import cn.spear.event.business.domain.enums.AppTypeEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 应用实体
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
@TableName(value = "app")
public class AppPO extends BasePO {
    @TableId
    private Long id;

    /**
     * 应用类型
     */
    private AppTypeEnum type;

    /**
     * 唯一标志，如 myapp，这个应用下的所有事件必须为 myapp.xxx.xxx
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * logo
     */
    private String logo;

    /**
     * 租户 ID
     */
    private String privateId;

    private Integer sort;

    /**
     * 配置项
     */
    private Integer options;
}
