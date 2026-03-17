package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.ClubMember;
import com.mvsr.mvsrconnect.model.ClubRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    Optional<ClubMember> findByUserIdAndClubId(Long userId, Long clubId);

    List<ClubMember> findByClubId(Long clubId);

    List<ClubMember> findByClubIdAndRole(Long clubId, ClubRole role);

    List<ClubMember> findByUserId(Long userId);

}