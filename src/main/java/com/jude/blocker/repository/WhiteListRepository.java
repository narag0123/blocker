package com.jude.blocker.repository;

import com.jude.blocker.entity.WhiteListEntry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
public interface WhiteListRepository extends JpaRepository<WhiteListEntry, Long> {
    /** plate가 화이트리스트에 존재하는지 여부 */
    boolean existsByPlate(String plate);

    /** plate로 엔티티 단건 조회 (필요 시 owner 등 함께 보려면 사용) */
    Optional<WhiteListEntry> findByPlate(String plate);
}
