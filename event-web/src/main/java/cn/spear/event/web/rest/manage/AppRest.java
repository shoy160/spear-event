package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.AppCreateDTO;
import cn.spear.event.business.domain.dto.AppDTO;
import cn.spear.event.business.domain.dto.AppPageDTO;
import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.business.service.AppService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.cmd.AppCmd;
import cn.spear.event.web.model.cmd.AppEditCmd;
import cn.spear.event.web.model.cmd.OptionsCmd;
import cn.spear.event.web.model.vo.PageVO;
import cn.spear.event.web.rest.BaseManageRest;
import cn.hutool.core.bean.BeanUtil;
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
@RequiredArgsConstructor
@RestController("ManageApp")
@RequestMapping("manage/app")
@Api(value = "应用管理", tags = "应用管理接口")
public class AppRest extends BaseManageRest {
    private final AppService appService;

    @PostMapping
    @ApiOperation(value = "创建应用", notes = "创建应用")
    public boolean create(@Valid @RequestBody AppCmd cmd) {
        AppCreateDTO dto = toBean(cmd, AppCreateDTO.class);
        return Objects.nonNull(appService.create(dto));
    }

    @GetMapping
    @ApiOperation(value = "获取应用分页", notes = "获取应用分页")
    public PagedDTO<AppPageDTO> page(
            @Valid PageVO pageVO,
            @ApiParam(value = "应用类型") @RequestParam(required = false) AppTypeEnum type,
            @ApiParam(value = "私有 ID") @RequestParam(required = false) String privateId,
            @ApiParam(value = "应用编码") @RequestParam(required = false) String code,
            @ApiParam(value = "排序方式") @RequestParam(required = false) SortWayEnum sortWay
    ) {
        return appService.findPaged(pageVO.getPage(), pageVO.getSize(), type, privateId, code, sortWay);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "应用详情", notes = "应用详情")
    public AppDTO getDetail(@PathVariable Long id) {
        return appService.getDetail(id);
    }

    @PutMapping("{id}")
    @ApiOperation(value = "应用编辑", notes = "应用编辑")
    public boolean edit(@PathVariable Long id, @RequestBody @Valid AppEditCmd cmd) {
        AppDTO app = BeanUtil.toBean(cmd, AppDTO.class);
        return appService.edit(id, app);
    }

    @GetMapping("list")
    @ApiOperation(value = "应用列表", notes = "应用列表")
    public List<AppDTO> getList(
            @ApiParam(value = "私有 ID") @RequestParam(required = false) String privateId,
            @ApiParam(value = "应用类型") @RequestParam(required = false) AppTypeEnum type,
            @ApiParam(value = "应用编码") @RequestParam(required = false) String code
    ) {
        return appService.getList(type, privateId, code);
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "应用删除", notes = "应用删除")
    public Boolean remove(@PathVariable Long id) {
        return appService.deleteById(id);
    }

    @PutMapping("{id}/options")
    @ApiOperation(value = "应用配置", notes = "应用配置")
    public boolean options(@PathVariable Long id, @RequestBody OptionsCmd cmd
    ) {
        return appService.setOptions(id, cmd.getOptions(), cmd.getAdd());
    }
}
