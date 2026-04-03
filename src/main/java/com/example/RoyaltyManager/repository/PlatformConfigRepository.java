package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {
    List<PlatformConfig> findByActive(Boolean active);
}
