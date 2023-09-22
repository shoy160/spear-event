package cn.spear.event.web.serializer.json;

import cn.spear.event.core.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 处理字符串等类型的null值
 *
 * @author luoyong
 * @date 2021/6/24
 */
public class NullStringJsonSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeString(Constants.STR_EMPTY);
    }
}
