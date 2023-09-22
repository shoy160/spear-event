package cn.spear.event.business.strategy;

import lombok.Data;

/**
 * @author songxueyan
 * @date 2023/3/29
 **/
@Data
public class Condition {

    private String field;
    private String op;
    private String value;

    public OpaOperate opType(){
        switch (op.toUpperCase()){
            case "IN" : return OpaOperate.IN;
            case "NOTIN" : return OpaOperate.NOTIN;
            default: return OpaOperate.OTHER;
        }
    }
}

