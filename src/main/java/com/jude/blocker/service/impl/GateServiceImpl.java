package com.jude.blocker.service.impl;

import com.jude.blocker.dto.GateCmdRequest;
import com.jude.blocker.entity.GateEvent;
import com.jude.blocker.repository.BlackListRepository;
import com.jude.blocker.repository.GateEventRepository;
import com.jude.blocker.repository.WhiteListRepository;
import com.jude.blocker.service.GateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GateServiceImpl implements GateService {

    private final WhiteListRepository whitelistRepo;
    private final BlackListRepository blacklistRepo;
    private final GateEventRepository eventRepo;

    @Value("${app.esp.base-url:}")
    private String espBaseUrl;                       // 예: http://192.168.0.50
    @Value("${app.gate.cooldown-ms:8000}")
    private long cooldownMs;

    private final HttpClient http = HttpClient.newHttpClient();

    // 중복 오픈 방지용 상태
    private volatile long   lastOpenAt = 0L;
    private volatile String lastPlate  = null;

    @Override
    public Map<String, Object> handleMobileOn(GateCmdRequest req) {
        final String action = req.getAction()==null? "" : req.getAction().trim().toUpperCase();
        final String plate  = req.getPlate()==null ? "" : req.getPlate().trim();

        if (!"ON".equals(action)) {
            return Map.of("ok", false, "error", "invalid_action (use ON)");
        }

        // 1) 블랙리스트 우선 차단
        if (!plate.isBlank() && blacklistRepo.existsByPlate(plate)) {
            saveEvent(GateEvent.Source.MOBILE, GateEvent.Action.SKIP, plate, null);
            return Map.of("ok", false, "reason", "blacklisted");
        }

        // 2) 특정 차량만 통과 → 화이트리스트 필수
        if (plate.isBlank() || !whitelistRepo.existsByPlate(plate)) {
            saveEvent(GateEvent.Source.MOBILE, GateEvent.Action.SKIP, plate.isBlank()? null : plate, null);
            return Map.of("ok", false, "reason", "not_in_whitelist");
        }

        // 3) 쿨다운(같은 차량 연속 열림 방지)
        long now = System.currentTimeMillis();
        if (plate.equals(lastPlate) && (now - lastOpenAt < cooldownMs)) {
            return Map.of("ok", false, "reason", "cooldown");
        }

        // 4) ESP32 호출 (연결되었다고 가정하되, 실패해도 서버는 동작)
        boolean espOk = callEspOpen(true);

        // 5) 상태 갱신 + 로그 저장
        lastOpenAt = now;
        lastPlate  = plate;
        saveEvent(GateEvent.Source.MOBILE, GateEvent.Action.OPEN, plate, null);

        // 6) 응답
        if (!espOk) {
            // 동작 우선: ESP 미연결이어도 API는 성공 + 경고
            return Map.of("ok", true, "warn", "esp_unreachable");
        }
        return Map.of("ok", true);
    }

    private boolean callEspOpen(boolean open) {
        try {
            if (espBaseUrl == null || espBaseUrl.isBlank()) {
                // 아직 ESP 설정 전 – 호출하지 않고 false 리턴
                return false;
            }
            var req = HttpRequest.newBuilder()
                .uri(URI.create(espBaseUrl + "/gate/cmd"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"open\":" + open + "}"))
                .build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (Exception e) {
            System.err.println("[ESP] call failed: " + e.getMessage());
            return false;
        }
    }

    private void saveEvent(GateEvent.Source src, GateEvent.Action act, String plate, Double conf) {
        eventRepo.save(GateEvent.builder()
            .source(src).action(act).plate(plate)
            .confidence(conf).ts(Instant.now()).build());
    }
}