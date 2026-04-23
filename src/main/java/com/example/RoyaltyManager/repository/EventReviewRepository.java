package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.EventReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventReviewRepository extends JpaRepository<EventReview, Long> {
    List<EventReview> findByEventId(Long eventId);
}
