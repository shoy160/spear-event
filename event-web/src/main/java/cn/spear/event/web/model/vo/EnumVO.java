package cn.spear.event.web.model.vo;

import cn.spear.event.core.domain.dto.BaseDTO;
import cn.spear.event.core.domain.dto.EntryDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luoyong
 * @date 2022/12/14
 */
@Getter
@Setter
@ToString
public class EnumVO extends BaseDTO {
    private String name;
    private List<EntryDTO<Object, String>> values;

    public EnumVO() {
        values = new ArrayList<>();
    }
}
