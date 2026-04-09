package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.FoodStall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodStallRepository extends JpaRepository<FoodStall, Long> {
    List<FoodStall> findByActiveTrue();

    Optional<FoodStall> findBySetupTokenHash(String hash);
}
