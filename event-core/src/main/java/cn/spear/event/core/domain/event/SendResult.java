package cn.spear.event.core.domain.event;

import lombok.*;

/**
 * @author luoyong
 * @date 2022/11/8
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {
    private String messageId;
    private String topic;
}
