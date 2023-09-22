package cn.spear.event.web.rest.app;

import cn.spear.event.business.service.ConfigService;
import cn.spear.event.web.rest.BaseAppRest;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 事件中心
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Slf4j
@RequiredArgsConstructor
@RestController("ConfigApp")
@RequestMapping("app/config")
@Api(value = "配置管理", tags = "配置管理")
public class ConfigRest extends BaseAppRest {
    private final ConfigService configService;
    private static final String APP_CONFIG_PREFIX = "public.";

    @GetMapping("map")
    @ApiOperation(value = "配置 Map", notes = "配置 Map")
    public Map<String, String> map(
            @ApiParam(value = "配置前缀", required = true) @RequestParam() String prefix
    ) {
        prefix = StrUtil.isBlank(prefix)
                ? APP_CONFIG_PREFIX
                : APP_CONFIG_PREFIX.concat(prefix);
        return configService.getMapConfig(prefix);
    }

    @GetMapping()
    @ApiOperation(value = "获取配置", notes = "获取配置")
    public Object get(
            @ApiParam(value = "配置 Key", required = true) @RequestParam String key
    ) {
        key = APP_CONFIG_PREFIX.concat(key);
        return configService.getObject(key);
    }
}
