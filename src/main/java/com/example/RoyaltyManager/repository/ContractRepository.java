package com.example.RoyaltyManager.repository;
import com.example.RoyaltyManager.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> { }