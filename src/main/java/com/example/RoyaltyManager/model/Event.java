package com.example.RoyaltyManager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private String venue;
    
    @Transient
    private Integer calculatedTicketsSold; // Transient field for performance pre-calculation

    @Column(nullable = false)
    private Double ticketPrice;

    @Column(nullable = false)
    private Integer totalCapacity = 500; // Default 500 seats

    // Many events can feature one primary artist
    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TicketPurchase> ticketPurchases;

    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<EventReview> reviews;

    public Event() {}

    public Event(String name, LocalDate eventDate, String venue, Double ticketPrice, Artist artist) {
        this.name = name;
        this.eventDate = eventDate;
        this.venue = venue;
        this.ticketPrice = ticketPrice;
        this.artist = artist;
        this.totalCapacity = 500;
    }

    public Event(String name, LocalDate eventDate, String venue, Double ticketPrice, Integer totalCapacity, Artist artist) {
        this.name = name;
        this.eventDate = eventDate;
        this.venue = venue;
        this.ticketPrice = ticketPrice;
        this.totalCapacity = totalCapacity;
        this.artist = artist;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getTicketsSold() {
        if (calculatedTicketsSold != null) return calculatedTicketsSold;
        if (ticketPurchases == null) return 0;
        return ticketPurchases.stream().mapToInt(tp -> tp.getQuantity() != null ? tp.getQuantity() : 0).sum();
    }
    
    public void setCalculatedTicketsSold(Integer calculatedTicketsSold) {
        this.calculatedTicketsSold = calculatedTicketsSold;
    }

    public boolean isSoldOut() {
        return getTicketsSold() >= (totalCapacity != null ? totalCapacity : 500);
    }

    public int getAvailableSeats() {
        int sold = getTicketsSold();
        int cap = totalCapacity != null ? totalCapacity : 500;
        return Math.max(0, cap - sold);
    }

    public List<TicketPurchase> getTicketPurchases() {
        return ticketPurchases;
    }

    public void setTicketPurchases(List<TicketPurchase> ticketPurchases) {
        this.ticketPurchases = ticketPurchases;
    }

    public List<EventReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<EventReview> reviews) {
        this.reviews = reviews;
    }

    public EventReview getPastReview() {
        if (reviews == null || reviews.isEmpty()) return null;
        return reviews.get(0);
    }
}
