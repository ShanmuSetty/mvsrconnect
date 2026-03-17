package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.ClubJoinRequest;
import com.mvsr.mvsrconnect.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, Long> {

    List<ClubJoinRequest> findByClubId(Long clubId);

    List<ClubJoinRequest> findByStatus(RequestStatus status);

    List<ClubJoinRequest> findByClub_IdAndStatus(Long clubId, RequestStatus status);

    Optional<ClubJoinRequest> findByUser_IdAndClub_IdAndStatus(
            Long userId,
            Long clubId,
            RequestStatus status
    );
}