package cn.spear.event.web.serializer.json;

import cn.spear.event.core.enums.TimestampType;
import cn.spear.event.core.utils.TypeUtils;
import cn.spear.event.web.config.BaseProperties;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author luoyong
 * @date 2021/6/7
 */
@RequiredArgsConstructor
public class SpearBeanSerializerModifier extends BeanSerializerModifier {

    private final BaseProperties baseConfig;

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        //循环所有的beanPropertyWriter
        for (BeanPropertyWriter writer : beanProperties) {
            //判断字段的类型，如果是array，list，set则注册nullSerializer
            Class<?> clazz = writer.getType().getRawClass();
            if (TypeUtils.isArray(clazz)) {
                writer.assignNullSerializer(new NullArrayJsonSerializer());
            } else if (TypeUtils.isNumber(clazz)) {
                writer.assignNullSerializer(new NullNumberJsonSerializer());
            } else if (TypeUtils.isBoolean(clazz)) {
                writer.assignNullSerializer(new NullBooleanJsonSerializer());
            } else if (TypeUtils.isString(clazz)) {
                writer.assignNullSerializer(new NullStringJsonSerializer());
            } else {
                if (TypeUtils.isDate(clazz)) {
                    if (baseConfig.getTimestamp() == TimestampType.None) {
                        writer.assignNullSerializer(new NullStringJsonSerializer());
                    } else {
                        writer.assignNullSerializer(new NullNumberJsonSerializer());
                    }
                } else {
                    writer.assignNullSerializer(new NullObjectJsonSerializer());
                }
            }
        }
        return beanProperties;
    }
}
