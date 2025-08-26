package com.jude.blocker.service.impl;

import com.jude.blocker.dto.AnprEventRequest;
import com.jude.blocker.dto.GateCmdRequest;
import com.jude.blocker.entity.GateEvent;
import com.jude.blocker.repository.BlackListRepository;
import com.jude.blocker.repository.GateEventRepository;
import com.jude.blocker.repository.WhiteListRepository;
import com.jude.blocker.service.EspClient;
import com.jude.blocker.service.GateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * 게이트 제어 핵심 비즈니스 로직
 *
 * - /gate/cmd (모바일/웹 수동 제어): action("ON"/"OFF")만 받아 ESP에 전달, 화이트/블랙리스트 체크 없음
 * - /anpr/event (OpenCV ANPR): plate(+confidence) 기반으로 화이트/블랙/신뢰도/쿨다운 체크 후 ESP 제어
 *
 * 응답 포맷(예시):
 *   성공: { "ok": true }
 *   성공(ESP 미연결/실패 경고): { "ok": true, "warn": "esp_unreachable" }
 *   실패: { "ok": false, "reason": "<사유>" }
 */
@Service
@RequiredArgsConstructor
public class GateServiceImpl implements GateService {

    // === 의존성 ===
    private final WhiteListRepository whitelistRepo;
    private final BlackListRepository blacklistRepo;
    private final GateEventRepository eventRepo;
    private final EspClient espClient;

    // === 설정값 (application.properties / 환경변수) ===
    @Value("${app.gate.cooldown-ms:8000}")
    private long cooldownMs;              // 연속 오픈 방지(중복 요청 완화)

    @Value("${app.anpr.min-confidence:0.85}")
    private double minConf;               // ANPR 최소 신뢰도 기준

    // === 상태 메모리(간단한 쿨다운용) ===
    // 모바일에서는 plate 개념이 없으므로 "최근 열림 시각"만 기록
    private volatile long lastMobileOpenAt = 0L;

    // ANPR은 최근 열린 plate와 시각을 함께 기록하여 중복 오픈 방지
    private volatile long lastAnprOpenAt = 0L;
    private volatile String lastAnprPlate = null;

    // ------------------------------------------------------------------
    // 모바일/웹 수동 제어: action("ON"/"OFF")만 들어옴. 화이트/블랙 체크 없음
    // ------------------------------------------------------------------
    @Override
    public Map<String, Object> processMobileCommand(GateCmdRequest req) {
        // action 정규화
        final String action = req.getAction() == null ? "" : req.getAction().trim().toUpperCase();

        if (!("ON".equals(action) || "OFF".equals(action))) {
            return Map.of("ok", false, "reason", "invalid_action");
        }

        // "ON"일 때만 쿨다운 적용 (연속 열림 방지)
        if ("ON".equals(action)) {
            long now = System.currentTimeMillis();
            if (now - lastMobileOpenAt < cooldownMs) {
                return Map.of("ok", false, "reason", "cooldown");
            }
        }

        // ESP 호출 (연결되어 있지 않아도 서버는 동작해야 하므로 실패해도 경고만)
        boolean ok = espClient.sendOpen("ON".equals(action));
        if ("ON".equals(action)) {
            lastMobileOpenAt = System.currentTimeMillis();
        }

        // 이벤트 로그 저장 (plate 없음)
        saveEvent(GateEvent.Source.MOBILE,
            "ON".equals(action) ? GateEvent.Action.OPEN : GateEvent.Action.CLOSE,
            null, null);

        // 호출 결과 반환
        return ok ? Map.of("ok", true) : Map.of("ok", true, "warn", "esp_unreachable");
    }

    // ------------------------------------------------------------------
    // ANPR(OpenCV) 이벤트: plate(+confidence) 기반 정책(화이트/블랙/신뢰도/쿨다운)
    // ------------------------------------------------------------------
    @Override
    public Map<String, Object> processAnprEvent(AnprEventRequest ev) {
        final String plate = ev.getPlate() == null ? "" : ev.getPlate().trim();
        final double conf  = ev.getConfidence() == null ? 0.0 : ev.getConfidence();

        // 입력값 검증
        if (plate.isBlank()) {
            return Map.of("ok", false, "reason", "empty_plate");
        }
        if (conf < minConf) {
            saveSkip(plate, conf);
            return Map.of("ok", false, "reason", "low_confidence");
        }

        // 블랙리스트 우선 차단
        if (blacklistRepo.existsByPlate(plate)) {
            saveSkip(plate, conf);
            return Map.of("ok", false, "reason", "blacklisted");
        }

        // 특정 차량만 통과 → 화이트리스트 존재 여부 확인
        if (!whitelistRepo.existsByPlate(plate)) {
            saveSkip(plate, conf);
            return Map.of("ok", false, "reason", "not_in_whitelist");
        }

        // plate 기반 쿨다운(같은 차량이 짧은 시간 내 중복 인식될 수 있음)
        long now = System.currentTimeMillis();
        if (plate.equals(lastAnprPlate) && (now - lastAnprOpenAt < cooldownMs)) {
            return Map.of("ok", false, "reason", "cooldown");
        }

        // ESP 열기
        boolean ok = espClient.sendOpen(true);
        lastAnprOpenAt = now;
        lastAnprPlate  = plate;

        // 이벤트 로그 저장
        saveEvent(GateEvent.Source.ANPR, GateEvent.Action.OPEN, plate, conf);

        return ok ? Map.of("ok", true) : Map.of("ok", true, "warn", "esp_unreachable");
    }

    // ------------------------------------------------------------------
    // 유틸: 이벤트 저장(OPEN/CLOSE/SKIP)
    // ------------------------------------------------------------------
    private void saveEvent(GateEvent.Source src, GateEvent.Action act, String plate, Double conf) {
        eventRepo.save(GateEvent.builder()
            .source(src)
            .action(act)
            .plate(plate)           // 모바일의 경우 null
            .confidence(conf)       // ANPR일 때만 값 존재
            .ts(Instant.now())
            .build());
    }

    // ANPR에서 정책 미통과 시 SKIP 기록
    private void saveSkip(String plate, Double conf) {
        saveEvent(GateEvent.Source.ANPR, GateEvent.Action.SKIP, plate, conf);
    }
}