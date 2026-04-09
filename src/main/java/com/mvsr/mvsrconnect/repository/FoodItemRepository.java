package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByStallId(Long stallId);
    List<FoodItem> findByStallIdAndAvailableTrue(Long stallId);
}