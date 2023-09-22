package cn.spear.event.business.domain.dto;

import cn.spear.event.core.domain.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 自定义应用发布实体
 *
 * @author luoyong
 * @date 2023/4/13
 */
@Getter
@Setter
@ToString
public class AppPublishDTO extends BaseDTO {
    private Long appId;
    private String appCode;
    private String appName;
    private String appLogo;
    private List<String> categories;
    private List<String> groups;
}
