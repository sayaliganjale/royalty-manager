package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByArtistId(Long artistId);
}
