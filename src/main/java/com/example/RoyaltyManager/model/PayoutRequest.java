package com.example.RoyaltyManager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_requests")
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    private Double amount;
    private String bankDetails; // Simple text description
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private LocalDateTime requestedAt = LocalDateTime.now();
    private String adminNote;

    public PayoutRequest() {}

    public PayoutRequest(Artist artist, Double amount, String bankDetails) {
        this.artist = artist;
        this.amount = amount;
        this.bankDetails = bankDetails;
        this.requestedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getBankDetails() { return bankDetails; }
    public void setBankDetails(String bankDetails) { this.bankDetails = bankDetails; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
