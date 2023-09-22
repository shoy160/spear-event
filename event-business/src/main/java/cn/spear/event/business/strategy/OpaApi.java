package cn.spear.event.business.strategy;

import cn.spear.event.core.http.HttpHelper;
import cn.spear.event.core.http.HttpRequest;
import cn.spear.event.core.http.HttpResponse;
import cn.spear.event.core.http.enums.HttpContentType;
import lombok.extern.slf4j.Slf4j;

/**
 * @author songxueyan
 * @date 2023/3/29
 **/
@Slf4j
public class OpaApi {

    HttpRequest httpRequest;
    private static final OpaConfig config = new OpaConfig();
    // Create or Update a Policy
    public static void createPolicy(Strategy strategy){
        String url = String.format("%s/v1/policies/%s",config.getHost(),strategy.getStrategyId());
        HttpResponse response = HttpHelper.put(url, strategy.toRego(config));
        System.out.println(response.readBody());
    }

    public static boolean verifyInput(String strategyId,Object data){
        config.getRegion().split(".");
        String url = String.format("%s/v1/data/%s/%s/%s",
                config.getHost(),
                config.getRegion().replace(".","/"),
                strategyId,
                config.getResult());
        log.info("verifyInput: url={}",url);
        //log.info("verifyInput: data="+data.toString());
        HttpResponse response = HttpHelper.post(url, data, HttpContentType.Json);
        OpaResult result = response.readBodyT(OpaResult.class);
        System.out.println(result);
        return result.getResult();
    }
}
