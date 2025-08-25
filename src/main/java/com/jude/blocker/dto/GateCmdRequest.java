package com.jude.blocker.dto;

import lombok.*;

/**
 * 모바일에서 게이트 제어 요청을 받을 때 사용되는 DTO.
 *
 * 예시 JSON:
 * {
 *   "action": "ON",
 *   "plate": "12가3456"
 * }
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GateCmdRequest {
    /**
     * 제어 동작
     * - "ON" : 게이트 열기
     * - "OFF": 게이트 닫기 (유지형 릴레이일 경우)
     */
    private String action;

    /**
     * 차량 번호 (선택)
     * - 특정 차량만 허용 정책일 때 화이트리스트/블랙리스트 검증에 사용
     * - 예: "12가3456"
     */
    private String plate;
}