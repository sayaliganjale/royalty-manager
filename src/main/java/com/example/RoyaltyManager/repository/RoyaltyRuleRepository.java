package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.RoyaltyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoyaltyRuleRepository extends JpaRepository<RoyaltyRule, Long> {
    List<RoyaltyRule> findByActive(Boolean active);
    List<RoyaltyRule> findByPlatformName(String platformName);
}
