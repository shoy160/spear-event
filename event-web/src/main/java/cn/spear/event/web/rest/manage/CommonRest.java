package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.AuthInfoDTO;
import cn.spear.event.business.service.ConfigService;
import cn.spear.event.core.Constants;
import cn.spear.event.core.annotation.EnableAuth;
import cn.spear.event.core.cache.Cache;
import cn.spear.event.core.domain.dto.EntryDTO;
import cn.spear.event.core.utils.EnumUtils;
import cn.spear.event.web.config.BaseProperties;
import cn.spear.event.web.model.vo.EnumVO;
import cn.spear.event.web.rest.BaseManageRest;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/12/14
 */
@RequiredArgsConstructor
@RestController("ManageCommon")
@RequestMapping("manage/common")
@Api(value = "通用接口管理", tags = "通用接口管理")
public class CommonRest extends BaseManageRest {

    private static final String ENUM_CACHE_KEYS = "enum:constants";

    private final ConfigService configService;
    private final BaseProperties config;
    private final Cache<String, List<EnumVO>> cache;

    @GetMapping("config")
    @EnableAuth(anonymous = true)
    @ApiOperation(value = "配置信息", notes = "配置信息")
    public Map<String, Object> config() {
        BaseProperties.AuthConfig authConfig = this.config.getAuthConfig();
        Map<String, Object> map = BeanUtil.beanToMap(authConfig);
        map.remove("appSecret");
        return map;
    }

    @GetMapping("config/auth")
    @EnableAuth(anonymous = true)
    @ApiOperation(value = "认证配置信息", notes = "认证配置信息")
    public AuthInfoDTO authConfig(
            @ApiParam(value = "配置环境") @RequestParam(required = false) String profile
    ) {
        return configService.get(Constants.AUTH_CONFIG, profile, AuthInfoDTO.class);
    }

    @PutMapping("config/auth")
    @ApiOperation(value = "认证配置信息修改", notes = "认证配置信息修改")
    public Boolean updateConfig(
            @ApiParam(value = "配置环境") @RequestParam(required = false) String profile,
            @RequestBody AuthInfoDTO value
    ) {
        return configService.save(Constants.AUTH_CONFIG, value, profile);
    }

    @GetMapping("enums")
    @EnableAuth(anonymous = true)
    @ApiOperation(value = "枚举字典", notes = "枚举字典")
    public List<EnumVO> enumList(
            @ApiParam(value = "枚举健，多个以逗号(,)分割") @RequestParam(required = false) String key,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh
    ) {
        List<EnumVO> enumList = null;
        if (!Objects.equals(true, refresh)) {
            enumList = cache.get(ENUM_CACHE_KEYS);
        }
        if (Objects.isNull(enumList)) {
            Map<String, Map<Object, String>> enums = EnumUtils.findEnums(config.getEnumSerializer());
            enumList = new ArrayList<>();
            for (Map.Entry<String, Map<Object, String>> entry : enums.entrySet()) {
                EnumVO item = new EnumVO();
                item.setName(entry.getKey());
                List<EntryDTO<Object, String>> enumValues = EntryDTO.ofMap(entry.getValue());
                item.setValues(enumValues);
                enumList.add(item);
            }
            cache.put(ENUM_CACHE_KEYS, enumList, 10, TimeUnit.MINUTES);
        }
        if (CollUtil.isEmpty(enumList)) {
            return enumList;
        }

        return enumList.stream().filter(t -> {
            if (StrUtil.isBlank(key)) {
                return true;
            }
            return ArrayUtil.contains(key.split(Constants.STR_SPLIT), t.getName());
        }).collect(Collectors.toList());
    }
}
