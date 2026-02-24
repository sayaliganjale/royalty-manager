package com.example.RoyaltyManager.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;

@Entity
@Table(name = "royalty_transactions")
public class RoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "track_name")
    private String songTitle;

    @CsvBindByName(column = "artist_name")
    private String platformArtistName;

    @CsvBindByName(column = "stream_count")
    private long streamCount;

    @CsvBindByName(column = "label")
    private String platform;

    private double grossRevenue;
    private double reportedPayout;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    // --- Constructor ---
    public RoyaltyTransaction() {}

    // --- Logic Methods ---
    public void calculateRevenue() {
        // Spotify approx rate $0.004
        this.grossRevenue = this.streamCount * 0.004;
    }

    public double getCalculatedExpected() {
        if (artist == null) return 0.0;
        return (this.grossRevenue * artist.getContractSplit()) / 100.0;
    }

    public double getDiscrepancy() {
        return getCalculatedExpected() - this.reportedPayout;
    }

    // --- GETTERS AND SETTERS (Sab Yahan Hain) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }

    public String getPlatformArtistName() { return platformArtistName; }
    public void setPlatformArtistName(String platformArtistName) { this.platformArtistName = platformArtistName; }

    public long getStreamCount() { return streamCount; }
    public void setStreamCount(long streamCount) { this.streamCount = streamCount; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public double getGrossRevenue() { return grossRevenue; }
    public void setGrossRevenue(double grossRevenue) { this.grossRevenue = grossRevenue; }

    public double getReportedPayout() { return reportedPayout; }
    public void setReportedPayout(double reportedPayout) { this.reportedPayout = reportedPayout; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

} // Ye final bracket hai jo class ko band karta hai.