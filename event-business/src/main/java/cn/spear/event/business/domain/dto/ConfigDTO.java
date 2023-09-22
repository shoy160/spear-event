package cn.spear.event.business.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * todo
 *
 * @author shay
 * @date 2023/4/15
 **/
@Getter
@Setter
@ToString
public class ConfigDTO extends BaseDateDTO {
    private Long id;
    private String key;
    private String value;
}
