package cn.spear.event.sdk.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author luoyong
 * @date 2023/2/9
 */
@Getter
@RequiredArgsConstructor
public enum eventEventType {
    /**
     * 公共事件
     */
    Public(1, "公共事件"),
    /**
     * 私有事件
     */
    Private(2, "私有事件");

    private final int value;
    private final String text;
}
