package cn.spear.event.core.session;

/**
 * @author shay
 * @date 2021/3/4
 */
public interface TenantSolver {
    /**
     * 获取租户ID
     *
     * @return tenantId
     */
    Object getTenantId();
}
