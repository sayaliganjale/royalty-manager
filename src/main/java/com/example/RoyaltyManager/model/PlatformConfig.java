package com.example.RoyaltyManager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_config")
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String platformName;

    private String platformType; // STREAMING, DOWNLOAD, LIVE

    private Double baseRatePerStream = 0.004;

    private String primaryRegion;

    private Boolean active = true;

    private String logoUrl;

    private String apiEndpoint;

    public PlatformConfig() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }

    public Double getBaseRatePerStream() { return baseRatePerStream; }
    public void setBaseRatePerStream(Double baseRatePerStream) { this.baseRatePerStream = baseRatePerStream; }

    public String getPrimaryRegion() { return primaryRegion; }
    public void setPrimaryRegion(String primaryRegion) { this.primaryRegion = primaryRegion; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
}
