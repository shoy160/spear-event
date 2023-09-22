package cn.spear.event.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * todo
 *
 * @author shay
 * @date 2020/8/15
 */
@Slf4j
public class StreamUtils {
    public static InputStream fromArray(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }

    public static byte[] array(InputStream inStream) {
        if (inStream == null) {
            return null;
        }
        try (ByteArrayOutputStream swapStream = new ByteArrayOutputStream()) {
            final int step = 1024;
            byte[] buff = new byte[step];
            int rc;
            while ((rc = inStream.read(buff, 0, step)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        } catch (IOException ex) {
            log.warn("array 转换异常", ex);
            return null;
        }
    }
}
