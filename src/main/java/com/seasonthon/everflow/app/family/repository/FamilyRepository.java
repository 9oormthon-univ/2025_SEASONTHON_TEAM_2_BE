package com.seasonthon.everflow.app.family.repository;

import com.seasonthon.everflow.app.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    /**
     * 초대 코드로 가족을 조회합니다.
     * @param inviteCode 조회할 6자리 숫자 초대 코드
     * @return 해당 초대 코드에 해당하는 Family 객체 (Optional)
     */
    Optional<Family> findByInviteCode(String inviteCode);
}
