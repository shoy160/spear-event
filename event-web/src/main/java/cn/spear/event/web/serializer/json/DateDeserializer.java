package cn.spear.event.web.serializer.json;

import cn.spear.event.core.enums.TimestampType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Date;

/**
 * @author luoyong
 * @date 2021/6/24
 */
@RequiredArgsConstructor
public class DateDeserializer extends JsonDeserializer<Date> {
    private final TimestampType type;

    @Override
    public Date deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        long timestamp = type == TimestampType.Second ? p.getLongValue() * 1000 : p.getLongValue();
        return new Date(timestamp);
    }
}
