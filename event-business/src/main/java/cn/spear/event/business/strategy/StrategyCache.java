package cn.spear.event.business.strategy;

import cn.spear.event.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author songxueyan
 * @date 2023/3/29
 **/
public class StrategyCache {

    //"cat" in input.user.pet
    // input.user.gender == "M"

    public static Strategy get(String id){
        Strategy strategy = new Strategy();
        strategy.setStrategyId("id001");

        Condition c1 = new Condition();
        c1.setField("user.pet");
        c1.setOp("in");
        c1.setValue("cat");
        strategy.add(c1);

        Condition c2 = new Condition();
        c2.setField("user.gender");
        c2.setOp("==");
        c2.setValue("M");
        strategy.add(c2);
        return strategy;
    }

    public static void main(String[] args) {
        Strategy strategy = StrategyCache.get("id001");
        OpaApi.createPolicy(strategy);
        JsonNode node2 = JsonUtils.node("{\"modelId\":\"m001\",\"input\":{\"user\":{\"name\":\"song yang cong\",\"site\":[\"sxy21.cn\",\"sxy91.com\"],\"age\":18,\"height\":180,\"email\":\"sxy996@qq.com\",\"gender\":\"M\",\"pet\":[\"cat\",\"dog\",\"elephant\",\"peacock\"]}}}\n" +
                "OpaResult(result=false)");
        JsonNode node = JsonUtils.node("{\"input\":{\"user\":{\"name\":\"song yang cong\",\"site\":[\"sxy21.cn\",\"sxy91.com\"],\"age\":18,\"height\":180,\"email\":\"sxy996@qq.com\",\"gender\":\"M\",\"pet\":[\"cat\",\"dog\",\"elephant\",\"peacock\"]}}}");
        OpaApi.verifyInput(strategy.getStrategyId(),node2);
    }
}
