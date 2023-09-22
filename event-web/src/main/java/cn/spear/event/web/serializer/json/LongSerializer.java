package cn.spear.event.web.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author luoyong
 * @date 2021/6/24
 */
public class LongSerializer extends JsonSerializer<Long> {
    private final static int MAX_LENGTH = 15;

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (null == value) {
            jsonGenerator.writeNumber(0);
        } else {
            String stringValue = String.valueOf(value);
            if (stringValue.length() > MAX_LENGTH) {
                jsonGenerator.writeString(stringValue);
            } else {
                jsonGenerator.writeNumber(value);
            }
        }
    }
}
