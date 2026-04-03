package com.example.RoyaltyManager.model;

public class ArtistPayoutDTO {
    private String name;
    private long trackCount;
    private double grossEarnings;
    private double artistNetShare;
    private String status;

    public ArtistPayoutDTO(String name, long trackCount, double grossEarnings, double artistNetShare, String status) {
        this.name = name;
        this.trackCount = trackCount;
        this.grossEarnings = grossEarnings;
        this.artistNetShare = artistNetShare;
        this.status = status;
    }

    // Getters
    public String getName() { return name; }
    public long getTrackCount() { return trackCount; }
    public double getGrossEarnings() { return grossEarnings; }
    public double getArtistNetShare() { return artistNetShare; }
    public String getStatus() { return status; }
}
