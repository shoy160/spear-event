package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Getter
@Setter
@ToString
public class ClientGrantDTO extends GrantDTO {
    private Long id;
    private Long clientId;
    private StatusEnum status;
    private Date createdAt;
    private Date updatedAt;
}
