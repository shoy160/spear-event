package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.ClientBatchGrantDTO;
import cn.spear.event.business.domain.dto.ClientGrantDTO;
import cn.spear.event.business.domain.dto.GrantDTO;
import cn.spear.event.business.domain.enums.GrantTypeEnum;
import cn.spear.event.business.domain.enums.ResourcePatternEnum;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.service.ClientGrantService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.cmd.ClientGrantCmd;
import cn.spear.event.web.model.vo.PageVO;
import cn.spear.event.web.rest.BaseManageRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 客户端管理
 *
 * @author luoyong
 * @date 2022/11/10
 */
@RequiredArgsConstructor
@RestController("ManageClientGrant")
@RequestMapping("manage/client/grant")
@Api(value = "客户端授权管理", tags = "客户端授权管理")
public class ClientGrantRest extends BaseManageRest {
    private final ClientGrantService grantService;

    @GetMapping()
    @ApiOperation(value = "授权列表", notes = "授权列表")
    public PagedDTO<ClientGrantDTO> list(
            @Valid PageVO page,
            @ApiParam(value = "客户端 ID") @RequestParam(required = false) Long clientId,
            @ApiParam(value = "授权类型") @RequestParam(required = false) GrantTypeEnum grantType,
            @ApiParam(value = "匹配模式") @RequestParam(required = false) ResourcePatternEnum patternType,
            @ApiParam(value = "授权状态") @RequestParam(required = false) StatusEnum status
    ) {
        return grantService.grantPaged(
                clientId, grantType, patternType, status, page.getPage(), page.getSize()
        );
    }

    @PostMapping("{clientId}")
    @ApiOperation(value = "客户端授权", notes = "客户端授权")
    public boolean create(@PathVariable Long clientId, @Valid @RequestBody ClientGrantCmd cmd) {
        return grantService.batchCreateGrant(clientId, toBean(cmd, GrantDTO.class), cmd.getTopics());
    }

    @PostMapping
    @ApiOperation(value = "客户端批量授权", notes = "客户端批量授权")
    public boolean batchCreate(@RequestBody List<ClientBatchGrantDTO> source) {
        return grantService.batchClientGrant(source);
    }

    @DeleteMapping("{grantId}")
    @ApiOperation(value = "取消客户端授权", notes = "取消客户端授权")
    public boolean cancel(@PathVariable Long grantId) {
        return grantService.removeGrant(grantId);
    }
}
