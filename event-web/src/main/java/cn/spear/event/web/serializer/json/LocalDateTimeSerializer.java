package cn.spear.event.web.serializer.json;

import cn.spear.event.core.enums.TimestampType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @author luoyong
 * @date 2021/6/24
 */
@RequiredArgsConstructor
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    private final TimestampType type;

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (null == value) {
            gen.writeNumber(-1);
        } else {
            Instant instant = value.toInstant(OffsetDateTime.now().getOffset());
            long timestamp = type == TimestampType.Second ? instant.getEpochSecond() : instant.toEpochMilli();
            gen.writeNumber(timestamp);
        }
    }
}
