package cn.spear.event.business.domain.dto;

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
public class AppListDTO extends BaseDTO {
    /**
     * 租户 ID
     */
    private String privateId;
}
