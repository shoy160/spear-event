package cn.spear.event.core.connector.enums;

/**
 * @author luoyong
 * @date 2022/12/27
 */
public enum ConsumerState {
    /**
     * unknown
     */
    UNKNOWN,
    /**
     * Rebalance
     */
    REBALANCE,
    ACTIVE,
    DEAD,
    EMPTY
}
