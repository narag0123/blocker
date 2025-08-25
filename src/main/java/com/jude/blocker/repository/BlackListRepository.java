package com.jude.blocker.repository;

import com.jude.blocker.entity.BlackListEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 블랙리스트 기본 CRUD + plate 기반 조회/존재 체크
 */
public interface BlackListRepository extends JpaRepository<BlackListEntry, Long> {

    /** plate가 블랙리스트에 존재하는지 여부 */
    boolean existsByPlate(String plate);

    /** plate로 엔티티 단건 조회 (차단 사유 등 확인 시) */
    Optional<BlackListEntry> findByPlate(String plate);
}