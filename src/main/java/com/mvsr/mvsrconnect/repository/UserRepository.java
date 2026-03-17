package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByNameIgnoreCase(String name);
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE %:term% OR LOWER(u.email) LIKE %:term%")
    List<User> searchUsers(@Param("term") String term);

}
