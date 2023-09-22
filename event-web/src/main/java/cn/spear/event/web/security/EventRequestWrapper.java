package cn.spear.event.web.security;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * @author shoy
 * @date 2021/7/28
 */
@Slf4j
public class EventRequestWrapper extends HttpServletRequestWrapper {
    private byte[] body;

    public EventRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public byte[] getBody() {
        if (null == this.body) {

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                IoUtil.copy(getRequest().getInputStream(), outputStream);
                this.body = outputStream.toByteArray();
            } catch (IOException e) {
                this.body = new byte[0];
            }

        }
        return this.body;
    }

    @Override
    public ServletInputStream getInputStream() {
        byte[] body = getBody();
        final ByteArrayInputStream bufferStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {
                return bufferStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
