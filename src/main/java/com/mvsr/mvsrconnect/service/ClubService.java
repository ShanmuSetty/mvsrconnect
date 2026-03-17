package com.mvsr.mvsrconnect.service;

import com.mvsr.mvsrconnect.model.ClubRole;
import com.mvsr.mvsrconnect.model.Role;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.ClubMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ClubService {

    private final ClubMemberRepository clubMemberRepository;

    public ClubService(ClubMemberRepository clubMemberRepository){
        this.clubMemberRepository = clubMemberRepository;
    }

    public boolean isMember(Long userId, Long clubId){

        return clubMemberRepository
                .findByUserIdAndClubId(userId, clubId)
                .isPresent();
    }

    public boolean isModerator(Long userId, Long clubId){

        return clubMemberRepository
                .findByUserIdAndClubId(userId, clubId)
                .map(member ->
                        member.getRole() == ClubRole.MODERATOR ||
                                member.getRole() == ClubRole.PRESIDENT
                )
                .orElse(false);
    }

    public boolean canModerate(User user, Long clubId){

        if(user.getRole() == Role.ADMIN) return true;

        return isModerator(user.getId(), clubId);
    }
}