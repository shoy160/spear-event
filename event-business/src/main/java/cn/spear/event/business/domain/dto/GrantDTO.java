package cn.spear.event.business.domain.dto;

import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.core.Constants;
import cn.spear.event.core.domain.dto.BaseDTO;
import cn.spear.event.core.utils.EnumUtils;
import cn.hutool.core.util.StrUtil;
import lombok.*;

import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantDTO extends BaseDTO {
    private GrantTypeEnum type;
    /**
     * 授权模式
     */
    private ResourcePatternEnum patternType;
    private String host;
    private String topic;
    private String source;
    private String group;

    public String getHost() {
        return StrUtil.isBlank(this.host) ? Constants.STR_ANY : this.host;
    }

    public boolean isMatch(GrantTypeEnum grantType, String topic) {
        if (!EnumUtils.hasFlag(this.type, grantType)) {
            return false;
        }
        switch (this.patternType) {
            case Literal:
                return Objects.equals(topic, this.topic);
            case Prefixed:
                return StrUtil.isNotBlank(topic) && topic.startsWith(this.topic);
            default:
                return false;
        }
    }
}
