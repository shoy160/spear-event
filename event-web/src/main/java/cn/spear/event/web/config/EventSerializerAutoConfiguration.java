package cn.spear.event.web.config;

import cn.spear.event.core.Constants;
import cn.spear.event.core.enums.BaseEnum;
import cn.spear.event.core.enums.EnumSerializerType;
import cn.spear.event.core.enums.TimestampType;
import cn.spear.event.core.utils.ReflectUtils;
import cn.spear.event.web.serializer.converter.DateLongConverter;
import cn.spear.event.web.serializer.converter.DateStringConverter;
import cn.spear.event.web.serializer.converter.EnumConverter;
import cn.spear.event.web.serializer.json.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/**
 * 时间戳 配置
 *
 * @author shay
 * @date 2020/11/4
 */
@Configuration
public class EventSerializerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Converter<String, Date> dateConverter(BaseProperties config) {
        return new DateStringConverter(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Integer, BaseEnum> enumConverter() {
        return new EnumConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Long, Date> dateLongConverter(BaseProperties config) {
        return new DateLongConverter(config);
    }

    @Bean
    @Primary
    public ObjectMapper serializingObjectMapper(BaseProperties config) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        //忽略大小写
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        SimpleModule module = new SimpleModule();
        if (config.getEnumSerializer() != EnumSerializerType.String) {
            module.addSerializer(BaseEnum.class, new EnumSerializer(config));
            Set<Class<?>> enums = ReflectUtils.findClasses(Constants.BASE_PACKAGE, t -> BaseEnum.class.isAssignableFrom(t) && t.isEnum());
            for (Class<?> clazz : enums) {
                module.addDeserializer(clazz, new EnumDeserializer(config, clazz));
            }
        }

        //长整型处理
        if (config.isLongToString()) {
            module.addSerializer(Long.class, new LongSerializer());
            module.addSerializer(Long.TYPE, new LongSerializer());
        }
        objectMapper.registerModule(module);

        //时间类型处理
        TimestampType timestamp = config.getTimestamp();
        if (timestamp != TimestampType.None) {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(timestamp));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(timestamp));
            javaTimeModule.addSerializer(Date.class, new DateSerializer(timestamp));
            javaTimeModule.addDeserializer(Date.class, new DateDeserializer(timestamp));
            objectMapper.registerModule(javaTimeModule);
        } else {
            objectMapper.setDateFormat(new SimpleDateFormat(config.getDateFormat()));
        }
        //空值处理
        if (config.isEnableNullValue()) {
            SpearBeanSerializerModifier modifier = new SpearBeanSerializerModifier(config);
            SerializerFactory serializerFactory = objectMapper.getSerializerFactory().
                    withSerializerModifier(modifier);
            objectMapper.setSerializerFactory(serializerFactory);
        }
        return objectMapper;
    }
}
