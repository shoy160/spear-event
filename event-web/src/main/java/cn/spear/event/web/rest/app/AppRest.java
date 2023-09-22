package cn.spear.event.web.rest.app;

import cn.spear.event.business.domain.dto.AppCreateDTO;
import cn.spear.event.business.domain.dto.AppDTO;
import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.business.service.AppService;
import cn.spear.event.web.model.cmd.AppCustomCmd;
import cn.spear.event.web.rest.BaseAppRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 应用管理
 *
 * @author luoyong
 * @date 2023/02/09
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping({"app/app", "app/manage"})
@Api(value = "应用管理", tags = "应用管理接口")
public class AppRest extends BaseAppRest {
    private final AppService appService;

    @PostMapping
    @ApiOperation(value = "创建自定义应用", notes = "创建自定义应用")
    public boolean create(@Valid @RequestBody AppCustomCmd cmd) {
        AppCreateDTO dto = toBean(cmd, AppCreateDTO.class);
        dto.setCode(cmd.getIdentifier());
        dto.setType(AppTypeEnum.Custom);
        return Objects.nonNull(appService.create(dto));
    }

    @GetMapping
    @ApiOperation(value = "获取应用列表", notes = "获取应用列表")
    public List<AppDTO> list(
            @ApiParam(value = "应用类型") @RequestParam(required = false) AppTypeEnum type,
            @ApiParam(value = "应用配置") @RequestParam(required = false) Integer options,
            @ApiParam(value = "私有 ID") @RequestParam(required = false) String privateId
    ) {
        return appService.findByPrivateId(privateId, type, options);
    }
}
