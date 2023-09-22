package cn.spear.event.business.strategy;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songxueyan
 * @date 2023/3/29
 **/
@Data
public class Strategy {

    private String strategyId;
    // and or
    private List<Condition> conditions;

    public void add(Condition condition){
        if(this.conditions == null){
            this.conditions = new ArrayList<>();
        }
        if(condition !=null){
            this.conditions.add(condition);
        }
    }

    public String toRego(OpaConfig config){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s.%s\n", config.getRegion(), strategyId));
        int index = sb.length();
        sb.append(String.format("default %s = false\n",config.getResult()));
        sb.append("result {\n");
        Boolean containsIn = false;
        for (Condition c:conditions
             ) {
            switch (c.opType()){
                case IN:
                    containsIn = true;
                    sb.append(String.format("\t\"%s\" in input.%s\n", c.getValue(), c.getField()));
                    break;
                case NOTIN:
                    containsIn = true;
                    sb.append(String.format("\tnot \"%s\" in input.%s\n", c.getValue(), c.getField()));
                case OTHER:
                    sb.append(String.format("\tinput.%s %s \"%s\"\n", c.getField(), c.getOp(), c.getValue()));
            }
        }
        if(containsIn){
            sb.insert(index,"import future.keywords.in\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}

