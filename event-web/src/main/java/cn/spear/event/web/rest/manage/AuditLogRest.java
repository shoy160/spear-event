package cn.spear.event.web.rest.manage;

import cn.spear.event.business.domain.dto.AuditLogDTO;
import cn.spear.event.business.domain.dto.AuditLogQueryDTO;
import cn.spear.event.business.service.AuditLogService;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.web.model.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author leizeyu
 * @date 2023/2/28
 */
@Slf4j
@RestController
@RequestMapping("/manage/audit_log")
@Api(value = "审计日志", tags = "审计日志")
@RequiredArgsConstructor
public class AuditLogRest {

    private final AuditLogService auditLogService;

    @GetMapping("paged")
    @ApiOperation(value = "审计日志分页", notes = "审计日志分页")
    public PagedDTO<AuditLogDTO> getPage(
            @Valid PageVO pageVO,
            AuditLogQueryDTO query
    ) {
        return auditLogService.findPage(query,pageVO.getPage(),pageVO.getSize());
    }
}
