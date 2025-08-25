package com.jude.blocker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;



@Entity
@Table(
    name = "blacklist",
    indexes = {
        @Index(name = "idx_blacklist_plate", columnList = "plate", unique = true)
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BlackListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 차단 대상 차량 번호 (유니크)
     */
    @Column(nullable = false, length = 20, unique = true)
    private String plate;

    /**
     * 차단 사유(선택)
     */
    @Column(length = 255)
    private String reason;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
