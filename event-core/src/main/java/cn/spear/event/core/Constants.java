package cn.spear.event.core;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author luoyong
 * @date 2022/11/7
 */
public interface Constants {
    String APP_NAME = "spear-event";
    String VERSION = "1.0";
    String STR_EMPTY = "";
    String STR_LEVEL = ".";
    String STR_REGION = ":";
    String STR_ANY = "*";
    String STR_SPLIT = ",";
    String STR_SPACE = " ";

    String BASE_PACKAGE = "cn.spear.event";
    String REST_PACKAGE = "cn.spear.event.web.rest";
    String MAPPER_PACKAGE = "cn.spear.event.business.dao";

    String MODE_DEV = "dev";

    String MODE_TEST = "test";

    String MODE_READY = "ready";

    String MODE_PROD = "prod";

    String PROFILE_SECRET = "secret";

    List<String> MODE_LIST = Arrays.asList(MODE_DEV, MODE_TEST, MODE_READY, MODE_PROD);

    String AUTH_CONFIG = "event.auth.config";

    /**
     * 模式校验
     *
     * @param mode mode
     * @return valid
     */
    static boolean validMode(String mode) {
        return StrUtil.isNotEmpty(mode) && MODE_LIST.contains(mode);
    }

    String GROUP_APP = "app";
    String GROUP_MANAGE = "manage";

    String HEADER_AUTHORIZATION = "Authorization";

    String AUTHORIZATION_BASIC = "Basic";

    String CACHE_SYNC_CHANNEL = "_cache_sync";

    String ATTR_DELAY_MINUTES = "delayminutes";
    String ATTR_DELAY_HOURS = "delayhours";
    String ATTR_DELAY_DAYS = "delaydays";
    String ATTR_DELAY_SEND_TIME = "delayat";
    String ATTR_DELAY_TARGET_TOPIC = "eventtargettopic";
    String ATTR_PRIVATE_ID = "privateid";
    String KEY_DELAY_QUEUE = Constants.APP_NAME + ":delay:events";

    String ATTR_ID = "id";
    String ATTR_NAME = "name";


    String AUDIT_TARGET_DATA = "audit-target";
    String AUDIT_SOURCE_DATA = "audit-source";

    String CLAIM_PREFIX = "claim-";
    String CLAIM_USER_ID = "user-id";
    String CLAIM_TENANT_ID = "tenant-id";
    String CLAIM_USERNAME = "user-name";
    String CLAIM_ROLE = "role";
    String CLAIM_CLIENT_IP = "client-ip";
    String CLAIM_USER_AGENT = "user-agent";
    String CLAIM_GROUP = "group";

    String CLAIM_PRIVATE_ID = "private-id";
    String CLAIM_CLIENT_ID = "CLIENT_ID";
    String CLAIM_CLIENT_SECRET = "CLIENT_SECRET";

    String HEADER_PRIVATE_ID = "ce-privateid";
    String QUERY_PRIVATE_ID = "privateId";
    String SPEAR_PRIVATE_ID = "x-spear-private-id";

    String SESSION_TOKEN = "context-token";

    String HEADER_TENANT_ID = "tenant_id";
    String HEADER_LANGUAGE = "x-spear-language";

    int CODE_SUCCESS = 200;
    String HEADER_CONTENT_TYPE = "Content-Type";
    String HEADER_USER_AGENT = "User-Agent";
    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    String CONTENT_TYPE_FILE = "multipart/form-data";
    String CONTENT_TYPE_XML = "text/xml";
    String CONTENT_TYPE_HTML = "text/html";
}
