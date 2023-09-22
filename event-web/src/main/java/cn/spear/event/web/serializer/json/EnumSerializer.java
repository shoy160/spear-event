package cn.spear.event.web.serializer.json;

import cn.spear.event.core.enums.BaseEnum;
import cn.spear.event.core.enums.BaseNamedEnum;
import cn.spear.event.web.config.BaseProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author luoyong
 * @date 2021/6/30
 */
@RequiredArgsConstructor
public class EnumSerializer extends JsonSerializer<BaseEnum> {
    private final BaseProperties config;

    @Override
    public void serialize(BaseEnum baseEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        switch (config.getEnumSerializer()) {
            case String:
                jsonGenerator.writeString(baseEnum.toString());
                break;
            case Object:
                int code = baseEnum.getValue();
                String desc = baseEnum.toString();
                if (baseEnum instanceof BaseNamedEnum) {
                    desc = ((BaseNamedEnum) baseEnum).getName();
                }
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("code", code);
                jsonGenerator.writeStringField("desc", desc);
                jsonGenerator.writeEndObject();
                break;
            default:
                jsonGenerator.writeNumber(baseEnum.getValue());
                break;
        }
    }
}
