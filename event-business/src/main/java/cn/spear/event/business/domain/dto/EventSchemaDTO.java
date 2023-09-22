package cn.spear.event.business.domain.dto;

import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.*;

/**
 * @author leizeyu
 * @date 2023/2/24
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventSchemaDTO extends BaseDTO {

    private Long id;

    private String code;

    private String schema;
}
