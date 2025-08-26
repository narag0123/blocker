package com.jude.blocker.dto;

import lombok.*;

/**
 * 모바일에서 게이트 제어 요청을 받을 때 사용되는 DTO.
 * 예시 JSON:
 * {
 *   "action": "ON",
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

}