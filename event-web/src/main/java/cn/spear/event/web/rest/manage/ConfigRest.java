package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.ConfigDTO;
import cn.spear.event.business.service.ConfigService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.vo.PageVO;
import cn.spear.event.web.rest.BaseManageRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author luoyong
 * @date 2022/12/14
 */
@RequiredArgsConstructor
@RestController("ManageConfig")
@RequestMapping("manage/config")
@Api(value = "配置接口管理", tags = "配置接口管理")
public class ConfigRest extends BaseManageRest {

    private final ConfigService configService;

    @GetMapping("page")
    @ApiOperation(value = "配置分页", notes = "配置分页")
    public PagedDTO<ConfigDTO> config(
            @Valid PageVO pageVO,
            @ApiParam("配置 Key") @RequestParam(required = false) String key,
            @ApiParam("配置 Key 前缀") @RequestParam(required = false) String prefix,
            @ApiParam("配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.page(pageVO.getPage(), pageVO.getSize(), key, prefix, profile);
    }

    @GetMapping("keys")
    @ApiOperation(value = "配置 Keys", notes = "配置 Keys")
    public List<String> keys(
            @ApiParam(value = "配置 Key 前缀") @RequestParam(required = false) String prefix,
            @ApiParam(value = "配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.allKeys(prefix, profile);
    }

    @GetMapping("map")
    @ApiOperation(value = "配置 Map", notes = "配置 Map")
    public Map<String, String> map(
            @ApiParam(value = "配置 Key 前缀") @RequestParam(required = false) String prefix,
            @ApiParam(value = "配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.getMapConfig(prefix, profile);
    }

    @GetMapping()
    @ApiOperation(value = "获取配置", notes = "获取配置")
    public Object get(
            @ApiParam(value = "配置 Key", required = true) @RequestParam String key,
            @ApiParam(value = "配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.getObject(key, profile);
    }

    @PutMapping()
    @ApiOperation(value = "保存配置", notes = "保存配置")
    public Boolean save(
            @ApiParam(value = "配置 Key", required = true) @RequestParam String key,
            @ApiParam("配置值") @RequestBody() Object value,
            @ApiParam(value = "配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.save(key, value, profile);
    }

    @DeleteMapping()
    @ApiOperation(value = "保存配置", notes = "保存配置")
    public Boolean remove(
            @ApiParam(value = "配置 Key", required = true) @RequestParam String key,
            @ApiParam(value = "配置环境") @RequestHeader(required = false) String profile
    ) {
        return configService.remove(key, profile);
    }
}
