package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.RegionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegionRateRepository extends JpaRepository<RegionRate, Long> {
    Optional<RegionRate> findByRegionCode(String regionCode);
}
