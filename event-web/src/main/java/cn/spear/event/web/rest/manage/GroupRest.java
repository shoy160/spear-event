package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.GroupCreateDTO;
import cn.spear.event.business.domain.dto.GroupDTO;
import cn.spear.event.business.domain.dto.GroupDetailDTO;
import cn.spear.event.business.domain.dto.GroupQueryDTO;
import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.po.GroupPO;
import cn.spear.event.business.service.GroupService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.web.model.cmd.GroupCmd;
import cn.spear.event.web.model.cmd.GroupEditCmd;
import cn.spear.event.web.model.vo.GroupQueryVO;
import cn.spear.event.web.rest.BaseManageRest;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luoyong
 * @date 2022/11/7
 */
@RequiredArgsConstructor
@RestController("ManageGroup")
@RequestMapping("manage/group")
@Api(value = "分组标签管理", tags = "分组标签管理")
public class GroupRest extends BaseManageRest {
    private final GroupService groupService;

    @GetMapping()
    @ApiOperation(value = "分组标签分页", notes = "分组标签分页")
    public PagedDTO<GroupDetailDTO> list(
            @Valid GroupQueryVO queryVO
    ) {
        GroupQueryDTO queryDTO = toBean(queryVO, GroupQueryDTO.class);
        return groupService.findPaged(queryVO.getPage(), queryVO.getSize(), queryDTO);
    }

    @GetMapping("{groupId}")
    @ApiOperation(value = "分组标签详情", notes = "分组标签详情")
    public GroupDTO detail(@PathVariable Long groupId) {
        GroupPO model = groupService.getById(groupId);
        if (Objects.nonNull(model)) {
            return toBean(model, GroupDTO.class);
        }
        throw new BusinessException("分组标签不存在");
    }

    @PostMapping
    @ApiOperation(value = "创建分组标签", notes = "创建分组标签")
    public boolean create(@Valid @RequestBody GroupCmd cmd) {
        GroupCreateDTO dto = toBean(cmd, GroupCreateDTO.class);
        return groupService.create(dto);
    }

    @PutMapping("{groupId}")
    @ApiOperation(value = "编辑分组标签", notes = "编辑分组标签")
    public boolean edit(@PathVariable Long groupId, @Valid @RequestBody GroupEditCmd cmd) {
        return groupService.edit(groupId, cmd.getName(), cmd.getLogo(), cmd.getSort());
    }

    @DeleteMapping("{groupId}")
    @ApiOperation(value = "删除分组标签", notes = "删除分组标签")
    public boolean remove(@PathVariable Long groupId) {
        return groupService.removeById(groupId);
    }

    @GetMapping("list")
    @ApiOperation(value = "分组标签列表", notes = "分组标签列表")
    public List<GroupDTO> getParentGroup(
            @ApiParam(value = "类型") @RequestParam GroupTypeEnum type,
            @ApiParam(value = "应用编码") @RequestParam(required = false) String appCode
    ) {
        return groupService.lambdaQuery()
                .select(GroupPO::getId, GroupPO::getName)
                .eq(GroupPO::getType, type)
                .eq(StrUtil.isNotBlank(appCode), GroupPO::getAppCode, appCode)
                .orderByDesc(GroupPO::getSort)
                .list().stream()
                .map(i -> BeanUtil.toBean(i, GroupDTO.class))
                .collect(Collectors.toList());

    }
}
