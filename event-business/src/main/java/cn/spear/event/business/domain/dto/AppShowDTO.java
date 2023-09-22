package cn.spear.event.business.domain.dto;

import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author leizeyu
 * @date 2023/3/2
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AppShowDTO extends BaseDTO {

    private String id;

    private String code;

    private String name;

    private String logo;

    public AppShowDTO(String code) {
        this.code = code;
    }

}

