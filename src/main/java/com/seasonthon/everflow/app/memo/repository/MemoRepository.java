package com.seasonthon.everflow.app.memo.repository;

import com.seasonthon.everflow.app.memo.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    Optional<Memo> findByFamilyId(Long familyId);
    boolean existsByFamilyId(Long familyId);

}
