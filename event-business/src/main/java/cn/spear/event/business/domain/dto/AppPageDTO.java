package cn.spear.event.business.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author leizeyu
 * @date 2023/2/28
 */
@Getter
@Setter
@ToString
public class AppPageDTO extends AppDTO {

    private Boolean allowDelete;

    public AppPageDTO() {
        this.allowDelete = true;
    }
}
