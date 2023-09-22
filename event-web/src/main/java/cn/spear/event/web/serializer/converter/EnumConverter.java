package cn.spear.event.web.serializer.converter;

import cn.spear.event.core.enums.BaseEnum;
import cn.spear.event.core.utils.EnumUtils;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

/**
 * @author luoyong
 * @date 2021/6/30
 */
public class EnumConverter implements Converter<Integer, BaseEnum> {

    @Override
    public BaseEnum convert(@NonNull Integer value) {
        return EnumUtils.getEnum(value, BaseEnum.class);
    }
}
