package cn.spear.event.web.rest;

import cn.spear.event.core.Constants;
import cn.spear.event.core.annotation.EnableAuth;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@EnableAuth(group = Constants.GROUP_MANAGE)
public class BaseManageRest extends BaseRest {
}
