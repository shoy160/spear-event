package cn.spear.event.web.model.cmd;

import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.core.Constants;
import cn.spear.event.core.domain.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/11
 */
@Getter
@Setter
@ApiModel
public class ClientGrantCmd extends BaseDTO {
    /**
     * 授权类型
     */
    @ApiModelProperty("授权类型")
    private GrantTypeEnum type;
    /**
     * 授权模式
     */
    @ApiModelProperty("匹配模式")
    private ResourcePatternEnum patternType;
    @ApiModelProperty("授权事件编码，支持前缀模式，如：user.")
    private List<String> topics;
    @ApiModelProperty("授权源")
    private String source;
    @ApiModelProperty("授权分组")
    private String group;
    @ApiModelProperty("授权主机，默认：*")
    private String host;

    public ClientGrantCmd() {
        this.patternType = ResourcePatternEnum.Literal;
        this.host = Constants.STR_ANY;
    }
}
