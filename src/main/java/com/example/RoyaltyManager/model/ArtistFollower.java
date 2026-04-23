package com.example.RoyaltyManager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "artist_followers")
public class ArtistFollower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    private String followerEmail;
    private String followerName;
    private LocalDateTime followedAt = LocalDateTime.now();

    public ArtistFollower() {}

    public ArtistFollower(Artist artist, String followerName, String followerEmail) {
        this.artist = artist;
        this.followerName = followerName;
        this.followerEmail = followerEmail;
        this.followedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public String getFollowerEmail() { return followerEmail; }
    public void setFollowerEmail(String followerEmail) { this.followerEmail = followerEmail; }
    public String getFollowerName() { return followerName; }
    public void setFollowerName(String followerName) { this.followerName = followerName; }
    public LocalDateTime getFollowedAt() { return followedAt; }
}
