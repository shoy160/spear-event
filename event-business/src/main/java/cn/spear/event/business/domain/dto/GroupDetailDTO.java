package cn.spear.event.business.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author leizeyu
 * @date 2023/2/24
 */
@Getter
@Setter
@ToString
public class GroupDetailDTO extends GroupDTO {

    private String appName;

    private Long appId;

    private String appLogo;


}
