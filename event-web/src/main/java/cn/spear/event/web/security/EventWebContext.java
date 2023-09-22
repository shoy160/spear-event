package cn.spear.event.web.security;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * 认证上下文，取出从网关传递过来的认证信息
 *
 * @author kissy
 */
@Slf4j
public class EventWebContext {
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    public static HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getResponse();
        }
        return null;
    }

    /**
     * 通过Header名称获取参数
     *
     * @param headerName name
     * @return value
     */
    public static String getRequestHeader(String headerName) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(headerName);
    }

    public static String currentUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String uri = request.getRequestURL().toString();
        Map<String, String[]> map = request.getParameterMap();
        return UriUtil.buildParams(uri, map);
    }

    public static byte[] getBody() {
        final String bodyKey = "CONTEXT_BODY";
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            //缓存
            Object body = request.getAttribute(bodyKey);
            if (body != null) {
                return (byte[]) body;
            }
            ServletInputStream stream = request.getInputStream();
            if (stream == null || stream.isFinished()) {
                return null;
            }
            byte[] buffer = IoUtil.readBytes(stream);
            request.setAttribute(bodyKey, buffer);
            return buffer;
        } catch (IOException ex) {
            log.warn("get body from request error", ex);
            return null;
        }
    }

    private static final String UNKNOWN = "unknown";
    private static final String IP_UTILS_FLAG = ",";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";

    private static String localIp;

    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        return getClientIp(request);
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = null;

        if (request == null) {
            return LOCALHOST_IP1;
        }
        Logger logger = LoggerFactory.getLogger(EventWebContext.class);
        try {
            ip = request.getHeader("X-Original-Forwarded-For");
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            //获取nginx等代理的ip
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("x-forwarded-for");
            }
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StrUtil.isEmpty(ip) || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            //兼容k8s集群获取ip
            if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                    if (StrUtil.isEmpty(localIp)) {
                        //根据网卡取本机配置的IP
                        InetAddress iNet;
                        try {
                            iNet = InetAddress.getLocalHost();
                            localIp = iNet.getHostAddress();
                        } catch (UnknownHostException e) {
                            logger.warn("getClientIp error: {}", e.getMessage());
                        }
                    }
                    ip = localIp;
                }
            }
        } catch (Exception e) {
            logger.warn("IPUtils ERROR ", e);
        }
        //使用代理，则获取第一个IP地址
        if (!StrUtil.isEmpty(ip) && ip.indexOf(IP_UTILS_FLAG) > 0) {
            ip = ip.substring(0, ip.indexOf(IP_UTILS_FLAG));
        }
        return ip;
    }

    public static Object getAttribute(String name) {
        HttpServletRequest request = getRequest();
        return null == request ? null : request.getAttribute(name);
    }

    public static <T> T getAttribute(String name, Class<T> clazz) {
        Object attribute = getAttribute(name);
        if (null == attribute) {
            return null;
        }
        return Convert.convert(clazz, attribute);
    }
}