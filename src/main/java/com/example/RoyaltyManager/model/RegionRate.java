package com.example.RoyaltyManager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "region_rates")
public class RegionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String regionCode; // US, EU, IN, AU, GLOBAL

    private String regionName; // United States, Europe, India, etc.

    private Double rateMultiplier = 1.0; // Multiplier on base rate

    private Double basePayoutUSD = 0.004;

    private String currency = "USD";

    private Boolean active = true;

    public RegionRate() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public Double getRateMultiplier() { return rateMultiplier; }
    public void setRateMultiplier(Double rateMultiplier) { this.rateMultiplier = rateMultiplier; }

    public Double getBasePayoutUSD() { return basePayoutUSD; }
    public void setBasePayoutUSD(Double basePayoutUSD) { this.basePayoutUSD = basePayoutUSD; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
