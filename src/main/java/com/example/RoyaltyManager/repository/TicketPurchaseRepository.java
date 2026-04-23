package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Long> {
    List<TicketPurchase> findByEventId(Long eventId);

    @Query("SELECT tp.event.id, SUM(tp.quantity) FROM TicketPurchase tp GROUP BY tp.event.id")
    List<Object[]> countTicketsSoldForEvents();
}
