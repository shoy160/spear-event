package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Getter
@Setter
@ToString
public class ClientDTO extends BaseDTO {
    private Long id;
    private String name;
    private String secret;
    private StatusEnum status;
}
