package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.EventSchemaDTO;
import cn.spear.event.business.service.EventService;
import cn.spear.event.web.handler.ResponseRaw;
import cn.spear.event.web.rest.BaseManageRest;
import cn.hutool.core.convert.Convert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author luoyong
 * @date 2022/11/7
 */
@RequiredArgsConstructor
@RestController("ManageEventSchema")
@RequestMapping("manage/event")
@Api(value = "事件 Schema 管理", tags = "事件 Schema 管理")
public class EventSchemaRest extends BaseManageRest {
    private final EventService eventService;

    @GetMapping(value = "{id}/schema")
    @ApiOperation(value = " 事件 Schema 查询", notes = "事件 Schema 查询")
    public EventSchemaDTO getSchema(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.getSchema(idValue);
        }
        return eventService.getSchema(id);
    }

    @PostMapping(value = "{id}/schema")
    @ApiOperation(value = "事件 Schema 更新", notes = "事件 Schema 更新")
    public boolean saveSchema(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.saveSchema(idValue, schema);
        }
        return eventService.saveSchema(id, schema);
    }

    @ResponseRaw
    @GetMapping(value = "{id}/schema/yaml", produces = {"application/yaml;charset=UTF-8"})
    @ApiOperation(value = "事件 Schema YAML 格式查询", notes = "事件 Schema YAML 格式查询")
    public String getSchemaYaml(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.getSchema(idValue).getSchema();
        }
        return eventService.getSchema(id).getSchema();
    }

    @PostMapping(value = "{id}/schema/yaml", consumes = {"application/yaml;charset=UTF-8"})
    @ApiOperation(value = "事件 Schema YAML 更新", notes = "事件 Schema YAML 更新")
    public boolean saveSchemaYaml(
            @ApiParam(value = "事件 ID 或编码", required = true) @PathVariable String id,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema
    ) {
        Long idValue = Convert.toLong(id, 0L);
        if (idValue > 0) {
            return eventService.saveSchema(idValue, schema);
        }
        return eventService.saveSchema(id, schema);
    }

    @PostMapping(value = "schema")
    @ApiOperation(value = "事件 Schema 更新 (code)", notes = "事件 Schema 更新")
    public boolean saveSchemaByCode(
            @ApiParam(value = "事件编码", required = true) @RequestParam String code,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema
    ) {
        return eventService.saveSchema(code, schema);
    }

    @PostMapping(value = "schema/yaml", consumes = {"application/yaml;charset=UTF-8"})
    @ApiOperation(value = "事件 Schema YAML 更新 (code)", notes = "事件 Schema YAML 更新")
    public boolean saveSchemaYamlByCode(
            @ApiParam(value = "事件编码", required = true) @RequestParam String code,
            @ApiParam(value = "事件 Schema", required = true) @RequestBody String schema
    ) {
        return eventService.saveSchema(code, schema);
    }
}
