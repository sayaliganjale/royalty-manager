package com.example.RoyaltyManager.repository;
import com.example.RoyaltyManager.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> { }