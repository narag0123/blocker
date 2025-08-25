package com.jude.blocker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "gate_events",
    indexes = {
        @Index(name = "idx_gate_events_ts", columnList = "ts"),
        @Index(name = "idx_gate_events_source", columnList = "source"),
        @Index(name = "idx_gate_events_plate", columnList = "plate")
    })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트 발생 출처: MOBILE / ANPR 등
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Source source;

    /**
     * 동작: OPEN / CLOSE / SKIP
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Action action;

    /**
     * 인식된 차량 번호(없으면 null)
     */
    @Column(length = 20)
    private String plate;

    /**
     * 인식 신뢰도(ANPR일 때만 채울 수 있음, 선택)
     */
    @Column
    private Double confidence;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant ts;

    // 편의 enum
    public enum Source { MOBILE, ANPR }
    public enum Action { OPEN, CLOSE, SKIP }
}
