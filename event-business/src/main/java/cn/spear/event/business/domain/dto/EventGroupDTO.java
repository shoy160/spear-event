package cn.spear.event.business.domain.dto;

import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.*;

import java.util.List;

/**
 * @author luoyong
 * @date 2023/2/16
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventGroupDTO extends BaseDTO {
    private String groupName;
    private List<EventDTO> events;
}
