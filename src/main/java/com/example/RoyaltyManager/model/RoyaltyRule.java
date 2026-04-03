package com.example.RoyaltyManager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "royalty_rules")
public class RoyaltyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleName;

    private String platformName;   // Spotify, YouTube, etc.

    private String contractType;   // EXCLUSIVE, NON_EXCLUSIVE, LABEL

    private String region;         // US, EU, ASIA, GLOBAL

    private Double artistSplitPercent = 70.0;

    private Double labelSplitPercent = 30.0;

    private Double minimumThreshold = 0.001; // Minimum per-stream payout

    private Boolean active = true;

    private String description;

    public RoyaltyRule() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getArtistSplitPercent() { return artistSplitPercent; }
    public void setArtistSplitPercent(Double artistSplitPercent) { this.artistSplitPercent = artistSplitPercent; }

    public Double getLabelSplitPercent() { return labelSplitPercent; }
    public void setLabelSplitPercent(Double labelSplitPercent) { this.labelSplitPercent = labelSplitPercent; }

    public Double getMinimumThreshold() { return minimumThreshold; }
    public void setMinimumThreshold(Double minimumThreshold) { this.minimumThreshold = minimumThreshold; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
