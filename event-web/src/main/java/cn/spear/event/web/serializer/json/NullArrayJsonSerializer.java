package cn.spear.event.web.serializer.json;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 处理数组类型的null值
 *
 * @author luoyong
 * @date 2021/6/24
 */
public class NullArrayJsonSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value == null) {
            gen.writeStartArray();
            gen.writeEndArray();
        }
    }
}