package com.jude.blocker.repository;
import com.jude.blocker.entity.GateEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게이트 이벤트 로그 기본 CRUD
 * (필요 시 기간/조건 조회 메서드 추가하면 됩니다)
 */
public interface GateEventRepository extends JpaRepository<GateEvent, Long> { }

