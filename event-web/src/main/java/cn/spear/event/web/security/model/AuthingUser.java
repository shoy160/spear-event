package cn.spear.event.web.security.model;

import cn.spear.event.core.domain.dto.BaseDTO;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author luoyong
 * @date 2023/3/3
 */
@Getter
@Setter
public class AuthingUser extends BaseDTO {
    private String userId;
    private String username;
    private String name;
    private String nickname;
    private String photo;
    private String gender;
    private Integer loginCount;
    private String lastIp;
    private Date lastLogin;
    private String status;

    public String getDisplayName() {
        return StrUtil.firstNonBlank(name, nickname, username);
    }
}
