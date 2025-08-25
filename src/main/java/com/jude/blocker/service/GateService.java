package com.jude.blocker.service;

import com.jude.blocker.dto.GateCmdRequest;
import java.util.Map;

public interface GateService {
    /** 모바일에서 오는 ON 요청 처리 (화이트/블랙, 쿨다운, ESP 호출, 로그 저장) */
    Map<String, Object> handleMobileOn(GateCmdRequest req);
}