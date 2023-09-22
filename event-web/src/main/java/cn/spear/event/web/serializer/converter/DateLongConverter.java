package cn.spear.event.web.serializer.converter;

import cn.spear.event.core.enums.TimestampType;
import cn.spear.event.web.config.BaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

/**
 * @author luoyong
 * @date 2021/6/21
 */
@RequiredArgsConstructor
public class DateLongConverter implements Converter<Long, Date> {
    private final BaseProperties config;

    @Override
    public Date convert(Long value) {
        TimestampType timestamp = config.getTimestamp();
        if (timestamp == TimestampType.Second) {
            return new Date(value * 1000);
        }
        return new Date(value);
    }
}
