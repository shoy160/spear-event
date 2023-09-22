package cn.spear.event.core.http.enums;

import cn.spear.event.core.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author shay
 * @date 2020/8/14
 */
@Getter
@AllArgsConstructor
public enum HttpContentType {
    /**
     * Json
     */
    Json(Constants.CONTENT_TYPE_JSON),
    Form(Constants.CONTENT_TYPE_FORM),
    File(Constants.CONTENT_TYPE_FILE),
    Xml(Constants.CONTENT_TYPE_XML),
    Html(Constants.CONTENT_TYPE_HTML);
    final String text;
}
