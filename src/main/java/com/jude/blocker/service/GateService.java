package com.jude.blocker.service;

import com.jude.blocker.dto.AnprEventRequest;
import com.jude.blocker.dto.GateCmdRequest;
import java.util.Map;

public interface GateService {
    /** 모바일에서 오는 ON 요청 처리 (화이트/블랙, 쿨다운, ESP 호출, 로그 저장) */
    // 모바일: action만 (plate 없음)
    Map<String, Object> processMobileCommand(GateCmdRequest req);

    // ANPR: plate(+confidence) 기반 처리
    Map<String, Object> processAnprEvent(AnprEventRequest ev);
}