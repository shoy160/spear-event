package cn.spear.event.core.connector.model;

import cn.spear.event.core.Constants;
import cn.spear.event.core.connector.enums.EventAclOperation;
import cn.spear.event.core.connector.enums.EventGrantType;
import cn.spear.event.core.connector.enums.EventPatternType;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luoyong
 * @date 2022/12/2
 */
@Getter
@Setter
public class EventAclBinding {
    private String user;
    private EventGrantType grantType;
    private EventAclOperation operation;
    private EventPatternType patternType;
    private String topic;
    private String group;
    private String host;

    public EventAclBinding() {
        this.host = Constants.STR_ANY;
        this.patternType = EventPatternType.Literal;
    }

    public String getHost() {
        return StrUtil.isBlank(this.host) ? Constants.STR_ANY : this.host;
    }
}
