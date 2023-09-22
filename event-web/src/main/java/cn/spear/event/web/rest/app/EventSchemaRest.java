package cn.spear.event.web.rest.app;

import cn.spear.event.business.domain.dto.EventSchemaDTO;
import cn.spear.event.business.service.EventService;
import cn.spear.event.web.handler.ResponseRaw;
import cn.spear.event.web.rest.BaseAppRest;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 事件管理
 *
 * @author luoyong
 * @date 2023/1/5
 */

@Slf4j
@RequiredArgsConstructor
@RestController("EventSchemaApp")
@RequestMapping("app/event")
@Api(value = "事件 Schema", tags = "事件 Schema 接口")
public class EventSchemaRest extends BaseAppRest {
    private final EventService eventService;

    @GetMapping("{id}/schema")
    @ApiOperation(value = " 事件 schema 查询", notes = "事件 schema 查询")
    public EventSchemaDTO getSchema(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.getSchema(idValue);
        }
        return eventService.getSchema(id);
    }

    @ResponseRaw
    @GetMapping(value = "{id}/schema/yaml", produces = {"application/yaml;charset=UTF-8"})
    @ApiOperation(value = " 事件 Schema YAML 查询", notes = "事件 Schema YAML 查询")
    public String getSchemaYaml(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable() String id
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.getSchema(idValue).getSchema();
        }
        return eventService.getSchema(id).getSchema();
    }

    @PostMapping(value = "{id}/schema")
    @ApiOperation(value = "事件 Schema 更新", notes = "事件 Schema 更新")
    public boolean saveSchema(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema,
            @ApiParam(value = "私有 ID") @RequestParam(required = false) String privateId
    ) {
        if (StrUtil.isBlank(privateId)) {
            privateId = privateId();
        }
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.saveSchema(idValue, schema, privateId);
        }
        return eventService.saveSchema(id, schema, privateId);
    }

    @PostMapping(value = "schema")
    @ApiOperation(value = "事件 Schema 更新", notes = "事件 Schema 更新")
    public boolean saveSchemaByCode(
            @ApiParam(value = "事件编码", required = true) @RequestParam String code,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema,
            @ApiParam(value = "私有 ID") @RequestParam(required = false) String privateId
    ) {
        if (StrUtil.isBlank(privateId)) {
            privateId = privateId();
        }
        return eventService.saveSchema(code, schema, privateId);
    }

    @GetMapping("schema")
    @ApiOperation(value = " 事件 schema 查询", notes = "事件 schema 查询")
    public EventSchemaDTO getSchemaByCode(
            @ApiParam(value = "事件编码", required = true) @RequestParam String code
    ) {
        return eventService.getSchema(code);
    }

    @ResponseRaw
    @GetMapping(value = "schema/yaml", produces = {"application/yaml;charset=UTF-8"})
    @ApiOperation(value = " 事件 Schema YAML 查询", notes = "事件 Schema YAML 查询")
    public String getSchemaYamlByCode(
            @ApiParam(value = "事件编码", required = true) @RequestParam String code
    ) {
        return eventService.getSchema(code).getSchema();
    }
}
