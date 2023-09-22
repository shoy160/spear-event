package cn.spear.event.business.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


/**
 * @author songxueyan
 * @date 2023/3/29
 **/
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "spear.strategy")
public class StrategyProperties {

    private ArrayList<String> sourceTopics;
    private String targetTopic = "spear.metadata.target";
}
