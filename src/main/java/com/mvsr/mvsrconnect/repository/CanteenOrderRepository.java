package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.CanteenOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CanteenOrderRepository extends JpaRepository<CanteenOrder, Long> {
    List<CanteenOrder> findByStallIdAndStatusIn(Long stallId, List<String> statuses);

    List<CanteenOrder> findByStudentUserId(Long userId);

    Optional<CanteenOrder> findByRazorpayPaymentLinkId(String linkId);

    @Query(value = "SELECT COALESCE(MAX(token_number), 0) + 1 FROM orders WHERE stall_id = :stallId AND DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    int nextTokenNumber(Long stallId);
}
