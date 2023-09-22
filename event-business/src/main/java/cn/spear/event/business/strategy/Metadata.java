package cn.spear.event.business.strategy;

import lombok.Data;

/**
 * @author songxueyan
 * @date 2023/3/29
 **/
@Data
public class Metadata {
    private String modelId;
    private Object input;
//    private JsonNode before;
//    private JsonNode after;
//    private JsonNode dis;
    private String targetTopic;
}
