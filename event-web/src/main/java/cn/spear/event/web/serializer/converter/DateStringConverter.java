package cn.spear.event.web.serializer.converter;

import cn.spear.event.core.enums.TimestampType;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.web.config.BaseProperties;
import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author luoyong
 * @date 2021/6/21
 */
@RequiredArgsConstructor
public class DateStringConverter implements Converter<String, Date> {
    private final BaseProperties config;

    @Override
    public Date convert(String value) {
        if (CommonUtils.isEmpty(value)) {
            return null;
        }
        Long timestamp = Convert.convert(Long.class, value, 0L);
        if (timestamp > 0) {
            return new Date(config.getTimestamp() == TimestampType.Second ? timestamp * 1000 : timestamp);
        }
        String dateFormat = config.getDateFormat();
        if (CommonUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        try {
            return format.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
