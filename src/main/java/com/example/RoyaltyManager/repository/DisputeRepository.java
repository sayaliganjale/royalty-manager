package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByArtistId(Long artistId);
}