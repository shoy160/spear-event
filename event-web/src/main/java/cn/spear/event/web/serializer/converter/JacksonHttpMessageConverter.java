package cn.spear.event.web.serializer.converter;

import cn.spear.event.web.config.BaseProperties;
import cn.spear.event.web.serializer.json.SpearBeanSerializerModifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author luoyong
 * @date 2021/6/7
 */
public class JacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public JacksonHttpMessageConverter(BaseProperties config) {
        ObjectMapper mapper = getObjectMapper();
        SpearBeanSerializerModifier modifier = new SpearBeanSerializerModifier(config);
        SerializerFactory serializerFactory = mapper.getSerializerFactory().withSerializerModifier(modifier);
        mapper.setSerializerFactory(serializerFactory);
    }
}
