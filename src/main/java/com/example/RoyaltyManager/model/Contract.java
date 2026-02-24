package com.example.RoyaltyManager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractType; // e.g., "Exclusive", "Distribution"
    private double royaltyPercentage; // Artist ko kitna milega (e.g., 50.0)
    private LocalDate startDate;
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name = "artist_id")
    private Artist artist; // Ek Artist ka ek hi active contract hoga

    // Getters and Setters generate kar lena
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public double getRoyaltyPercentage() { return royaltyPercentage; }
    public void setRoyaltyPercentage(double royaltyPercentage) { this.royaltyPercentage = royaltyPercentage; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
}