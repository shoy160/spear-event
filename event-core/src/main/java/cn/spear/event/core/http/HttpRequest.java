package cn.spear.event.core.http;

import cn.spear.event.core.http.enums.HttpContentType;
import cn.spear.event.core.http.enums.HttpMethod;
import cn.spear.event.core.lang.Func;
import cn.spear.event.core.utils.CommonUtils;
import cn.spear.event.core.utils.JsonUtils;
import cn.spear.event.core.utils.MapUtils;
import cn.spear.event.core.utils.TypeUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shay
 * @date 2020/8/14
 */
@Slf4j
@Getter
@Setter
public class HttpRequest {
    private String url;
    private HttpMethod method;
    private HttpContentType contentType;
    private String charset;
    private Object params;
    private Object data;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private Map<String, String> headers;
    private Map<String, byte[]> files;
    private Func<String, String> keyEditor;

    public HttpRequest(String url) {
        this(url, HttpMethod.GET, null);
    }

    public HttpRequest(String url, HttpMethod method) {
        this(url, method, null);
    }

    public HttpRequest(String url, HttpMethod method, Object data) {
        this.url = url;
        this.method = method;
        this.data = data;
        this.connectionTimeout = 15 * 1000;
        this.readTimeout = 15 * 1000;
        this.charset = "UTF-8";
        this.contentType = HttpContentType.Json;
        this.headers = new HashMap<>();
        this.files = new HashMap<>();
        this.headers.put("accept", "*/*");
        this.headers.put("connection", "Keep-Alive");
    }

    public String getUrl() {
        if (this.params == null) {
            return this.url;
        }
        String params;
        if (this.params instanceof String) {
            params = (String) this.params;
        } else {
            Map<String, Object> map = MapUtils.map(this.params, this.keyEditor);
            params = MapUtils.toUrl(map, this.charset);
        }
        if (CommonUtils.isEmpty(params)) {
            return this.url;
        }
        if (CommonUtils.isEmpty(this.url)) {
            return params;
        }
        String split = "?";
        if (this.url.indexOf(split) > 0) {
            split = "&";
        }
        return this.url.concat(split).concat(params);
    }

    public String getContentType(String boundary) {
        return String.format("%s; boundary=%s", this.contentType.getText(), boundary);
    }

    public byte[] getContent() {
        if (this.data == null) {
            return null;
        }
        String content;
        if (TypeUtils.isString(this.data)) {
            content = this.data.toString();
        } else {
            switch (contentType) {
//                case Xml:
//                    content = XmlUtils.serialize(this.data);
//                    break;
                case Json:
                    content = JsonUtils.toJson(this.data);
                    break;
                case Form:
                    content = MapUtils.toUrl(BeanUtil.beanToMap(this.data), this.charset);
                    break;
                default:
                    return null;
            }
        }
        if (content != null) {
            if (log.isDebugEnabled()) {
                log.debug("Http request body -> {}", content);
            }
            try {
                return content.getBytes(this.charset);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public void addFile(String path) {
        byte[] buffer = FileUtil.readBytes(path);
        String fileName = FileUtil.getName(path);
        addFile(fileName, buffer);
    }

    public void addFile(String fileName, byte[] fileData) {
        if (this.contentType != HttpContentType.File) {
            this.contentType = HttpContentType.File;
        }
        this.files.put(fileName, fileData);
    }

    public boolean hasFile() {
        return this.files != null && this.files.size() > 0;
    }

    public void writeFiles(OutputStream os, String boundary) {
        if (!hasFile()) {
            return;
        }
        //结尾标识
        byte[] endBuffer = String.format("\r\n--%s--\r\n", boundary).getBytes();
        //文件头
        String template = "\r\n--%s\r\nContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\nContent-Type:%s\r\n\r\n";
        try {
            for (String key : this.files.keySet()) {
                try {
                    String contentType = FileUtil.getMimeType(key);
                    if (CommonUtils.isEmpty(contentType)) {
                        continue;
                    }
                    String header = String.format(template, boundary, key, key, contentType);
                    System.out.println(header);
                    os.write(header.getBytes());
                    os.write(this.files.get(key));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            os.write(endBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }
}
