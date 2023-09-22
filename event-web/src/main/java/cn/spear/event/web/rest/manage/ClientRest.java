package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.ClientDTO;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.service.ClientService;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.cmd.ClientCmd;
import cn.spear.event.web.model.cmd.ClientEditCmd;
import cn.spear.event.web.model.vo.PageVO;
import cn.spear.event.web.rest.BaseManageRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 客户端管理
 *
 * @author luoyong
 * @date 2022/11/10
 */
@RequiredArgsConstructor
@RestController("ManageClient")
@RequestMapping("manage/client")
@Api(value = "客户端管理管理", tags = "客户端管理")
public class ClientRest extends BaseManageRest {
    private final ClientService clientService;

    @PostMapping
    @ApiOperation(value = "创建客户端", notes = "创建客户端")
    public ClientDTO create(@Valid @RequestBody ClientCmd cmd) {
        return clientService.create(cmd.getName(), cmd.getClientId(), cmd.getClientSecret());
    }

    @GetMapping
    @ApiOperation(value = "客户端列表", notes = "客户端列表")
    public PagedDTO<ClientDTO> list(
            @Valid PageVO paged,
            @ApiParam(value = "客户端名称") @RequestParam(required = false) String name
    ) {
        return clientService.paged(paged.getPage(), paged.getSize(), name);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "客户端详情", notes = "客户端详情")
    public ClientDTO getDetail(@PathVariable Long id) {
        return clientService.getDetail(id);
    }

    @PutMapping("{clientId}")
    @ApiOperation(value = "客户端编辑", notes = "客户端编辑")
    public boolean edit(@PathVariable Long clientId, @Valid @RequestBody ClientEditCmd cmd) {
        if (Objects.isNull(clientId)) {
            throw new RuntimeException("客户端 ID 不能为空");
        }
        return clientService.edit(clientId, cmd.getName());
    }

    @PutMapping("{clientId}/status")
    @ApiOperation(value = "更新状态", notes = "更新状态")
    public boolean updateStatus(@PathVariable Long clientId, StatusEnum status) {
        return clientService.updateStatus(clientId, status);
    }

    @PutMapping("{clientId}/secret")
    @ApiOperation(value = "客户端密钥更新", notes = "客户端密钥更新")
    public boolean updateSecret(@PathVariable Long clientId) {
        return clientService.updateSecret(clientId);
    }

    @GetMapping("{clientId}/config")
    @ApiOperation(value = "获取订阅配置(临时)", notes = "获取订阅配置(临时)")
    public Map<String, String> config(
            @PathVariable Long clientId,
            @RequestParam String event,
            @RequestParam(required = false) String privateId,
            @RequestParam(required = false) LanguageType language
    ) {
        language = Optional.ofNullable(language).orElse(LanguageType.Java);
        return this.clientService.consumerConfig(clientId, event, language, privateId);
    }

    @DeleteMapping("{clientId}")
    @ApiOperation(value = "客户端删除", notes = "客户端删除")
    public boolean delete(@PathVariable Long clientId) {
        return clientService.remove(clientId);
    }
}
