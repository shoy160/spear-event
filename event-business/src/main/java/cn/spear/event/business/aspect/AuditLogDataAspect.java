package cn.spear.event.business.aspect;

import cn.spear.event.business.annotation.AuditLog;
import cn.spear.event.business.domain.dto.AuditLogDTO;
import cn.spear.event.business.domain.enums.AuditOperationType;
import cn.spear.event.business.domain.enums.AuditResourceType;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.service.AuditLogService;
import cn.spear.event.core.Constants;
import cn.spear.event.core.session.Session;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author leizeyu
 * @date 2023/2/23
 */
@Component
@Aspect
@RequiredArgsConstructor
public class AuditLogDataAspect {

    private final Session session;
    private final AuditLogService auditLogService;


    @Around("@annotation(cn.spear.event.business.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog annotation = method.getAnnotation(AuditLog.class);
        AuditOperationType operation = annotation.operation();
        AuditResourceType resource = annotation.resource();
        AuditLogDTO auditLog = preprocessing(operation, resource);
        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } catch (Exception ex) {
            auditLog.setResult(false);
            throw ex;
        } finally {
            saveAuditLog(auditLog, operation, resource, joinPoint.getArgs());
        }
        return proceed;
    }

    private void saveAuditLog(
            AuditLogDTO auditLog, AuditOperationType operation, AuditResourceType resource,
            Object[] args
    ) {

        Map<String, Object> targetMap = compareDetail(auditLog);
        String sourceName = MapUtil.getStr(targetMap, Constants.ATTR_NAME);
        if (StrUtil.isBlank(sourceName)) {
            sourceName = MapUtil.getStr(targetMap, Constants.ATTR_ID);
        }
        if (operation.equals(AuditOperationType.Update)) {
            Optional<StatusEnum> statusOptional = Arrays.stream(args).
                    filter(t -> t instanceof StatusEnum)
                    .map(t -> (StatusEnum) t).findFirst();
            if (statusOptional.isPresent()) {
                auditLog.setDesc(String.format("%s%s[%s]", statusOptional.get().getName(), resource.getName(), sourceName));
            }
        }
        if (StrUtil.isBlank(auditLog.getDesc())) {
            auditLog.setDesc(String.format("%s%s[%s]", operation.getName(), resource.getName(), sourceName));
        }

        auditLogService.saveRecord(auditLog);
    }

    private Map<String, Object> compareDetail(AuditLogDTO auditLog) {
        Object source = session.getClaim(Constants.AUDIT_SOURCE_DATA);
        Object target = session.getClaim(Constants.AUDIT_TARGET_DATA);
        if (Objects.isNull(source) || Objects.isNull(target)) {
            return new HashMap<>(0);
        }
        Map<String, Object> sourceMap = BeanUtil.beanToMap(source);
        Map<String, Object> targetMap = BeanUtil.beanToMap(target);
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            String fieldName = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetValue = targetMap.get(fieldName);
            if (Objects.equals(sourceValue, targetValue)) {
                continue;
            }
            auditLog.recordChange(fieldName, sourceValue, targetValue);
        }
        return targetMap;
    }


    private AuditLogDTO preprocessing(AuditOperationType operation, AuditResourceType resource) {
        return AuditLogDTO.builder()
                .operationType(operation)
                .resourceType(resource)
                .creatorId(session.userIdAsString())
                .creatorName(session.getUserName())
                .clientIp(session.getClaimAsString(Constants.CLAIM_CLIENT_IP))
                .result(true)
                .build();
    }
}
