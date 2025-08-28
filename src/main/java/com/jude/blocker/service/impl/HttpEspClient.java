package com.jude.blocker.service.impl;

import com.jude.blocker.service.EspClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class HttpEspClient implements EspClient {

    @Value("${app.esp.base-url}")
    private String baseUrl;

    @Value("${app.esp.cmd-path:/cmd}")
    private String cmdPath;

    @Value("${app.esp.connect-timeout-ms:1000}")
    private int connectTimeoutMs;

    @Value("${app.esp.read-timeout-ms:2000}")
    private int readTimeoutMs;

    // ESP 파서(Postman과 동일)를 맞추려면 true → {"open":"true"} 형식
    @Value("${app.esp.open-as-string:true}")
    private boolean openAsString;

    private HttpClient client() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectTimeoutMs))
            .version(HttpClient.Version.HTTP_1_1) // chunked 회피
            .build();
    }

    @Override
    public boolean sendOpen(boolean open) {
        if (baseUrl == null || baseUrl.isBlank()) {
            System.err.println("[ESP] base-url not configured; skipping call");
            return false;
        }
        final String url = baseUrl + (cmdPath.startsWith("/") ? cmdPath : ("/" + cmdPath));

        final String json = openAsString
            ? "{\"open\":\"" + (open ? "true" : "false") + "\"}"
            : "{\"open\":" + open + "}";
        final byte[] body = json.getBytes(StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(readTimeoutMs))
            .version(HttpClient.Version.HTTP_1_1)
            .header("Content-Type", "application/json; charset=utf-8")
            // ❌ 금지 헤더들: Content-Length / Connection / Host / Expect 등 설정하지 않음
            .POST(HttpRequest.BodyPublishers.ofByteArray(body)) // CL 자동 설정
            .build();

        System.out.println("[ESP→] POST " + url + " CL=" + body.length + " body=" + json);
        try {
            HttpResponse<String> res = client().send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[ESP←] status=" + res.statusCode() + " body=" + res.body());
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (Exception e) {
            System.err.println("[ESP] call failed: " + e.getMessage() + " url=" + url);
            return false;
        }
    }
}