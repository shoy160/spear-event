package cn.spear.event.web.serializer.json;

import cn.spear.event.core.enums.TimestampType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * @author luoyong
 * @date 2021/6/24
 */
@RequiredArgsConstructor
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private final TimestampType type;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        Instant instant = type == TimestampType.Second
                ? Instant.ofEpochSecond(p.getLongValue())
                : Instant.ofEpochMilli(p.getLongValue());
        return LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId());
    }
}
