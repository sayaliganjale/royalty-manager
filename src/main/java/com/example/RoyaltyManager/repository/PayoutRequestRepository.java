package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.PayoutRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {
    List<PayoutRequest> findByArtistId(Long artistId);
    List<PayoutRequest> findByStatus(String status);
}
