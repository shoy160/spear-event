package cn.spear.event.core.connector;

/**
 * LifeCycle
 *
 * @author luoyong
 * @date 2022/11/8
 */
public interface LifeCycle {

    /**
     * 是否已启动
     *
     * @return boolean
     */
    boolean isStarted();

    /**
     * 是否已关闭
     *
     * @return boolean
     */
    boolean isClosed();

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void shutdown();
}
