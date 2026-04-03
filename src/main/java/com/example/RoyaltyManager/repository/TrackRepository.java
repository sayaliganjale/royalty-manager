package com.example.RoyaltyManager.repository;
import com.example.RoyaltyManager.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    long countByArtistId(Long artistId);
    List<Track> findByArtistId(Long artistId);
}
