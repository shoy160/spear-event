package cn.spear.event.core.http;

import cn.spear.event.core.Constants;
import cn.spear.event.core.http.enums.HttpContentType;
import cn.spear.event.core.http.enums.HttpMethod;
import cn.spear.event.core.utils.StreamUtils;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * HttpHelper
 *
 * @author shay
 * @date 2020/8/14
 */
@Slf4j
public class HttpHelper {
    /**
     * 发起Http请求
     *
     * @param request Http请求
     * @return Http响应
     */
    public static HttpResponse request(HttpRequest request) {
        HttpURLConnection connection = null;
        OutputStream os = null;
        HttpResponse response = new HttpResponse();
        try {
            //创建连接
            String url = request.getUrl();
            HttpMethod method = request.getMethod();
            if (log.isDebugEnabled()) {
                log.debug("Http Request -> [{}]{}", method, url);
            }

            connection = (HttpURLConnection) new URL(url).openConnection();
            //设置请求方式
            connection.setRequestMethod(method.toString());
            connection.setConnectTimeout(request.getConnectionTimeout());
            //设置连接超时时间
            connection.setReadTimeout(request.getReadTimeout());
            Map<String, String> header = request.getHeaders();
            for (String key : header.keySet()) {
                String value = header.get(key);
                connection.setRequestProperty(key, value);
                if (log.isDebugEnabled()) {
                    log.debug("Request Header -> {}:{}", key, value);
                }
            }
            String boundary = Constants.STR_EMPTY;
            if (request.hasFile()) {
                boundary = "---------------------------".concat(IdUtil.fastSimpleUUID());
                connection.setRequestProperty(Constants.HEADER_CONTENT_TYPE, request.getContentType(boundary));
            } else {
                connection.setRequestProperty(Constants.HEADER_CONTENT_TYPE, request.getContentType().getText());
            }
            connection.setDoInput(true);

            if (method != HttpMethod.GET) {
                //设置是否可读取
                connection.setDoOutput(true);
                //设置参数
                os = connection.getOutputStream();
                if (request.hasFile()) {
                    request.writeFiles(os, boundary);
                } else if (request.getData() != null) {
                    //拼装参数
                    os.write(request.getContent());
                }
            }
            connection.connect();
            int code = connection.getResponseCode();
            response.setCode(code);
            InputStream body;
            if (code == Constants.CODE_SUCCESS) {
                //获取返回的数据
                body = connection.getInputStream();
            } else {
                body = connection.getErrorStream();
            }
            response.setBody(StreamUtils.array(body));
            String contentType = connection.getHeaderField(Constants.HEADER_CONTENT_TYPE);
            response.setContentType(contentType);
            return response;
        } catch (IOException ex) {
            ex.printStackTrace();
            return response;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * get请求
     *
     * @param url url
     * @return Http响应
     */
    public static HttpResponse get(String url) {
        HttpRequest request = new HttpRequest(url);
        return request(request);
    }

    /**
     * get请求
     *
     * @param url url
     * @return Http响应
     */
    public static HttpResponse get(String url, Object params) {
        HttpRequest request = new HttpRequest(url);
        request.setParams(params);
        return request(request);
    }

    /**
     * post 请求
     *
     * @param url  url
     * @param data 数据
     * @return Http响应
     */
    public static HttpResponse post(String url, Object data) {
        HttpRequest request = new HttpRequest(url, HttpMethod.POST, data);
        return request(request);
    }

    /**
     * post 请求
     *
     * @param url  url
     * @param data 数据
     * @return Http响应
     */
    public static HttpResponse post(String url, Object data, HttpContentType type) {
        HttpRequest request = new HttpRequest(url, HttpMethod.POST, data);
        request.setContentType(type);
        return request(request);
    }

    /**
     * put 请求
     *
     * @param url  url
     * @param data 数据
     * @return Http响应
     */
    public static HttpResponse put(String url, Object data) {
        HttpRequest request = new HttpRequest(url, HttpMethod.PUT, data);
        return request(request);
    }

    /**
     * put 请求
     *
     * @param url  url
     * @param data 数据
     * @return Http响应
     */
    public static HttpResponse put(String url, Object data, HttpContentType type) {
        HttpRequest request = new HttpRequest(url, HttpMethod.PUT, data);
        request.setContentType(type);
        return request(request);
    }

    /**
     * delete 请求
     *
     * @param url url
     * @return Http响应
     */
    public static HttpResponse delete(String url, Object params) {
        HttpRequest request = new HttpRequest(url, HttpMethod.DELETE);
        request.setParams(params);
        return request(request);
    }

    /**
     * delete 请求
     *
     * @param url url
     * @return Http响应
     */
    public static HttpResponse delete(String url, Object data, HttpContentType type) {
        HttpRequest request = new HttpRequest(url, HttpMethod.DELETE, data);
        request.setContentType(type);
        return request(request);
    }
}
