package cn.spear.event.core.session;

import cn.spear.event.core.Constants;

import java.io.Closeable;

/**
 * @author shay
 * @date 2021/3/1
 */
public abstract class BaseSession implements Session {

    private SessionDTO tempSession;

    protected SessionDTO getTempSession() {
        return this.tempSession;
    }

    /**
     * 获取Session中TenantID
     *
     * @return tenantID
     */
    protected abstract Object getSessionTenantId();

    /**
     * 获取Session中约定
     *
     * @param key key
     * @return value
     */
    protected abstract Object getValue(String key);

    /**
     * 设置申明值
     *
     * @param key   key
     * @param value value
     */
    protected abstract void setValue(String key, Object value);

    @Override
    public Object getUserId() {
        if (this.tempSession != null) {
            return this.tempSession.getUserId();
        }
        return getValue(Constants.CLAIM_USER_ID);
    }

    @Override
    public Object getTenantId() {
        if (this.tempSession != null) {
            return this.tempSession.getTenantId();
        }
        return getSessionTenantId();
    }

    @Override
    public String getUserName() {
        if (this.tempSession != null) {
            return this.tempSession.getUserName();
        }
        Object value = getValue(Constants.CLAIM_USERNAME);
        return null == value ? null : value.toString();
    }

    @Override
    public String getRole() {
        if (this.tempSession != null) {
            return this.tempSession.getRole();
        }
        Object value = getValue(Constants.CLAIM_ROLE);
        return null == value ? null : value.toString();
    }

    @Override
    public Object getClaim(String key) {
        return getValue(Constants.CLAIM_PREFIX.concat(key));
    }

    @Override
    public void setClaim(String key, Object value) {
        setValue(Constants.CLAIM_PREFIX.concat(key), value);
    }

    @Override
    public Closeable use(SessionDTO session) {
        this.tempSession = session;
        return () -> tempSession = null;
    }
}
