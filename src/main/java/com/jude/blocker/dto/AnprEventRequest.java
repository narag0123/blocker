package com.jude.blocker.dto;
import lombok.*;

/**
 * OpenCV(ANPR) 인식 결과를 서버로 보낼 때 사용하는 DTO
 *
 * 예시 JSON:
 * {
 *   "plate": "12가3456",
 *   "confidence": 0.92
 * }
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AnprEventRequest {
    /** 인식된 차량번호 (예: 12가3456) */
    private String plate;

    /** 신뢰도(0.0 ~ 1.0), 필수는 아니지만 0.85 이상 권장 */
    private Double confidence;
}
