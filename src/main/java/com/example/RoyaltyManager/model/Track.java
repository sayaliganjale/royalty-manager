package com.example.RoyaltyManager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String isrcCode; // Unique ID for songs
    private LocalDate releaseDate;
    private String genre;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist; // Track kis artist ka hai

    // Getters and Setters generate kar lena (Right Click -> Source -> Generate Getters/Setters)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsrcCode() { return isrcCode; }
    public void setIsrcCode(String isrcCode) { this.isrcCode = isrcCode; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
}