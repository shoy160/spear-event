package cn.spear.event.web.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 处理布尔类型的null值
 *
 * @author luoyong
 * @date 2021/6/24
 */
public class NullBooleanJsonSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeBoolean(false);
    }
}
