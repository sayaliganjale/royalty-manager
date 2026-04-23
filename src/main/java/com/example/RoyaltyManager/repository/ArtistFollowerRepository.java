package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.ArtistFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArtistFollowerRepository extends JpaRepository<ArtistFollower, Long> {
    List<ArtistFollower> findByArtistId(Long artistId);
    boolean existsByArtistIdAndFollowerEmail(Long artistId, String followerEmail);
}
