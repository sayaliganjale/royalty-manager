package com.example.RoyaltyManager.repository;

import com.example.RoyaltyManager.model.RoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<RoyaltyTransaction, Long> {
    // This allows us to fetch all royalty logs from the database.
}