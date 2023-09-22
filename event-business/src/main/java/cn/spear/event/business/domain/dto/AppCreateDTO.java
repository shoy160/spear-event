package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author luoyong
 * @date 2022/11/15
 */
@Getter
@Setter
@ToString
public class AppCreateDTO extends BaseDTO {

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
     * 私有 ID
     */
    private String privateId;

    private Integer sort;
}
