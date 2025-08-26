package com.jude.blocker.service.impl;

import com.jude.blocker.service.EspClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP 기반 ESP 클라이언트
 * - app.esp.base-url: 예) http://192.168.0.50
 * - POST /gate/cmd {"open":true|false}
 */
@Component
public class HttpEspClient implements EspClient {

    @Value("${app.esp.base-url:}")
    private String baseUrl; // 비어 있으면 호출 스킵

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    @Override
    public boolean sendOpen(boolean open) {
        if (baseUrl == null || baseUrl.isBlank()) {
            System.err.println("[ESP] base-url not configured; skipping call");
            return false;
        }
        try {
            var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/gate/cmd"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"open\":" + open + "}"))
                .build();

            // 1차 시도
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 200 && res.statusCode() < 300) return true;

            // 간단 재시도 1회
            res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (Exception e) {
            System.err.println("[ESP] call failed: " + e.getMessage());
            return false;
        }
    }
}