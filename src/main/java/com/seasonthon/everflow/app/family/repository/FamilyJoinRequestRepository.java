package com.seasonthon.everflow.app.family.repository;

import com.seasonthon.everflow.app.family.domain.FamilyJoinRequest;
import com.seasonthon.everflow.app.family.domain.JoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyJoinRequestRepository extends JpaRepository<FamilyJoinRequest, Long> {

    List<FamilyJoinRequest> findAllByFamilyId(Long familyId);

    Optional<FamilyJoinRequest> findByFamilyIdAndUserId(Long familyId, Long userId);

    List<FamilyJoinRequest> findAllByFamilyIdAndStatus(Long familyId, JoinStatus status);

    void deleteAllByUserId(Long userId);
}
