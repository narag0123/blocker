package com.jude.blocker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.Instant;



@Entity
@Table(
    name = "whitelist",
    indexes = {
        @Index(name = "idx_whitelist_plate", columnList = "plate", unique = true)
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WhiteListEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 차량 번호 (유니크)
     * 예: "12가3456"
     */
    @Column(nullable = false, length = 20, unique = true)
    private String plate;

    /**
     * 소유자명(선택)
     */
    @Column(length = 100)
    private String owner;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
