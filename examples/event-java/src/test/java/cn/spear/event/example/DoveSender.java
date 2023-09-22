package cn.spear.event.example;

import cn.authing.event.core.utils.JsonUtils;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.http.impl.HttpMessageWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * todo
 *
 * @author shay
 * @date 2022/11/19
 **/
public class eventSender {

    @Test
    public void send() throws IOException {
        // 事件中台提供参数
        String eventHost = "http://localhost:8080";
        String eventTopic = "user.created";
        String clientId = "4168053424263168";
        String clientSecret = "c9db25d9b9f548968d06d7a7f56cd3ea";

        Map<String, Object> data = new HashMap<>(3);
        data.put("id", IdUtil.nanoId());
        data.put("name", "张纯");
        data.put("mobile", "13966666666");
        CloudEvent ceToSend = new CloudEventBuilder()
                .withId(IdUtil.nanoId())
                .withSubject(eventTopic)
                .withSource(URI.create("https://console.authing.cn/user/created"))
                .withType("java")
                .withDataContentType("application/json")
                .withData(JsonUtils.toJsonBuffer(data))
//                // 延迟消息 (四选一)
//                .withExtension("delayminutes", 5)
//                .withExtension("delayhours", 1)
//                .withExtension("delaydays", 2)
//                .withExtension("delayat", DateUtil.tomorrow().getTime())
                .build();
        URL url = new URL(String.format("%s/app/event/pub", eventHost));
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setRequestMethod("PUT");
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);

        String token = Base64.encode(String.format("%s:%s", clientId, clientSecret));
        httpUrlConnection.setRequestProperty("Authorization", String.format("Basic %s", token));
        HttpMessageWriter messageWriter = createMessageWriter(httpUrlConnection);
        messageWriter.writeBinary(ceToSend);
        httpUrlConnection.connect();
        byte[] body = IoUtil.readBytes(httpUrlConnection.getInputStream());
        System.out.printf("receive:%s%n", new String(body, StandardCharsets.UTF_8));
    }

    private static MessageReader createMessageReader(HttpURLConnection httpUrlConnection) throws IOException {
        Map<String, List<String>> headers = httpUrlConnection.getHeaderFields();

        byte[] body = IoUtil.readBytes(httpUrlConnection.getInputStream());
        return HttpMessageFactory.createReaderFromMultimap(headers, body);
    }

    private static HttpMessageWriter createMessageWriter(HttpURLConnection httpUrlConnection) {
        return HttpMessageFactory.createWriter(
                httpUrlConnection::setRequestProperty,
                body -> {
                    try {
                        if (body != null) {
                            httpUrlConnection.setRequestProperty("content-length", String.valueOf(body.length));
                            try (OutputStream outputStream = httpUrlConnection.getOutputStream()) {
                                outputStream.write(body);
                            }
                        } else {
                            httpUrlConnection.setRequestProperty("content-length", "0");
                        }
                    } catch (IOException t) {
                        throw new UncheckedIOException(t);
                    }
                });
    }

}
