package cn.spear.event.business.strategy;

import cn.spear.event.business.config.StrategyProperties;
import cn.spear.event.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;

/**
 * @author songxueyan
 * @date 2023/3/28
 **/
//@EnableKafkaStreams  暂时不上线
//@Component
@Slf4j
public class MetadataStream {

    private StrategyProperties strategyProperties;
    private OpaConfig config = new OpaConfig();

    public MetadataStream(StrategyProperties strategyProperties) {
        this.strategyProperties = strategyProperties;
        log.info("getTopics:{}",strategyProperties.getSourceTopics());

    }

    @Bean
    public KStream<String, String> KStream(StreamsBuilder streamsBuilder){
        if (strategyProperties.getSourceTopics() ==null || strategyProperties.getSourceTopics().isEmpty()){
            return null;
        }
        // TODO: 2023/3/30 判断 topic 是否创建，SourceTopics 不创建会报错，TargetTopic 不创建则不推送数据
        KStream<String,String> stream = streamsBuilder.stream(strategyProperties.getSourceTopics(),
                Consumed.with(Serdes.String(),Serdes.String()));
        stream.filter((k,v) -> myFilter(k,v))
                .peek((k,v) -> {log.info("v={}",v);})
                .to(strategyProperties.getTargetTopic());
        log.info("getTargetTopic:"+strategyProperties.getTargetTopic());
        return stream;
    }

    public boolean myFilter(String k,String v){
        JsonNode node = JsonUtils.node(v);
        Strategy strategy = StrategyCache.get(node.get("modelId").asText());
        try {
            log.info("getStrategyId:{}",strategy.getStrategyId());
            return OpaApi.verifyInput(strategy.getStrategyId(),node);
        }catch (Exception e){
            System.out.println(e);
        }
        return false;
    }
}
