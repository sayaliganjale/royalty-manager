package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Long> {
    List<TicketPurchase> findByEventId(Long eventId);
}
