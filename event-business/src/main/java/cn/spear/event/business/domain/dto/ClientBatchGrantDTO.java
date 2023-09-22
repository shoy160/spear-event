package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author leizeyu
 * @date 2023/3/6
 */
@Getter
@Setter
@ToString
public class ClientBatchGrantDTO extends BaseDTO {

    private String client;

    private List<Grant> grants;

    @Getter
    @Setter
    public static class Grant {
        private GrantTypeEnum type;
        private String group;
        private ResourcePatternEnum patternType;
        private List<String> topics;
    }
}
