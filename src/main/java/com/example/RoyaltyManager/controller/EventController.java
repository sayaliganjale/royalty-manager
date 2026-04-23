package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.dto.EventDTO;
import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.Event;
import com.example.RoyaltyManager.service.RoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;

@Controller
public class EventController {

    @Autowired
    private RoyaltyService royaltyService;

    @Autowired
    private com.example.RoyaltyManager.service.RealEmailService emailService;

    // Public Pages
    @GetMapping("/events")
    public String publicEventsPage(Model model) {
        model.addAttribute("events", royaltyService.getPublicEventDTOs());
        model.addAttribute("today", LocalDate.now());
        return "public_events";
    }

    @GetMapping("/music")
    public String musicHubPage() {
        return "music_library";
    }

    @GetMapping("/events/{id}/buy")
    public String buyTicketPage(@PathVariable Long id, Model model) {
        Event event = royaltyService.getEventById(id);
        if (event == null) {
            return "redirect:/events";
        }
        model.addAttribute("event", event);
        return "buy_ticket";
    }

    @PostMapping("/events/{id}/purchase")
    public String purchaseTicket(
            @PathVariable Long id,
            @RequestParam String buyerName,
            @RequestParam String buyerEmail,
            @RequestParam Integer quantity,
            @RequestParam String ticketType,
            @RequestParam Double amount,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        System.out.println("Processing ticket request for event: " + id + " | Buyer: " + buyerName);
        royaltyService.purchaseTicket(id, buyerName, buyerEmail, quantity, ticketType, amount);
        System.out.println("Purchase successful for: " + buyerEmail);
        
        Event event = royaltyService.getEventById(id);
        if (event != null) {
            String subject = "Your EliteStudio Ticket for " + event.getName();
            String body = "Dear " + buyerName + ",\n\n" +
                          "Thank you for your purchase!\n" +
                          "Event: " + event.getName() + " @ " + event.getVenue() + "\n" +
                          "Tickets: " + quantity + "x " + ticketType + "\n\n" +
                          "Get ready for an incredible experience!\nEliteStudio Support";
            emailService.sendEmail(buyerEmail, subject, body);
            
            // Forward real time attributes to success page securely (no URL manipulation)
            redirectAttributes.addFlashAttribute("event", event);
            redirectAttributes.addFlashAttribute("buyerName", buyerName);
            redirectAttributes.addFlashAttribute("buyerEmail", buyerEmail);
            redirectAttributes.addFlashAttribute("quantity", quantity);
            redirectAttributes.addFlashAttribute("ticketType", ticketType);
            redirectAttributes.addFlashAttribute("amount", amount);
        }
        
        return "redirect:/events/ticket-success";
    }

    @GetMapping("/events/ticket-success")
    public String ticketSuccessPage(Model model) {
        if (!model.containsAttribute("event")) {
            // Un-authenticated or direct URL hit, boot them back
            return "redirect:/events";
        }
        return "ticket_success";
    }

    // Admin Pages
    @GetMapping("/admin/events")
    public String manageEventsPage(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        List<Event> allEvents = royaltyService.getAllEvents();
        LocalDate today = LocalDate.now();
        
        List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> !e.getEventDate().isBefore(today))
                .toList();
                
        List<Event> pastEvents = allEvents.stream()
                .filter(e -> e.getEventDate().isBefore(today))
                .toList();
                
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("artists", royaltyService.getAllArtists());
        
        // Expose a helper to fetch artist event earnings
        model.addAttribute("royaltyService", royaltyService);
        
        return "manage_events";
    }

    @PostMapping("/admin/events/save")
    public String saveEvent(
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam String venue,
            @RequestParam Double ticketPrice,
            @RequestParam(defaultValue = "500") Integer totalCapacity,
            @RequestParam Long artistId,
            HttpSession session) {
        
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        
        List<Artist> artists = royaltyService.getAllArtists();
        Artist selectedArtist = artists.stream().filter(a -> a.getId().equals(artistId)).findFirst().orElse(null);
        
        if (selectedArtist != null) {
            Event event = new Event(name, eventDate, venue, ticketPrice, totalCapacity, selectedArtist);
            royaltyService.saveEvent(event);
            // Notify followers of this artist about the new event
            List<com.example.RoyaltyManager.model.ArtistFollower> followers = royaltyService.getFollowersByArtist(selectedArtist.getId());
            for (com.example.RoyaltyManager.model.ArtistFollower follower : followers) {
                String subject = "New Event Alert: " + selectedArtist.getName() + " is coming to " + venue + "!";
                String body = "Hey " + follower.getFollowerName() + ",\n\n" +
                              "Great news! " + selectedArtist.getName() + " just announced a new event!\n" +
                              "Event: " + name + "\nVenue: " + venue + "\nDate: " + eventDate + "\n\n" +
                              "Be quick — get your tickets before they sell out!\nEliteStudio";
                emailService.sendEmail(follower.getFollowerEmail(), subject, body);
            }
        }
        
        return "redirect:/admin/events";
    }

    // --- FAN CLUB: FOLLOW AN ARTIST ---
    @PostMapping("/events/follow")
    public String followArtist(
            @RequestParam Long artistId,
            @RequestParam String followerName,
            @RequestParam String followerEmail,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        
        com.example.RoyaltyManager.model.Artist artist = royaltyService.getAllArtists().stream()
                .filter(a -> a.getId().equals(artistId)).findFirst().orElse(null);
        
        if (artist != null) {
            royaltyService.followArtist(artist, followerName, followerEmail);
            ra.addFlashAttribute("followSuccess", "You are now following " + artist.getName() + "!");
        }
        return "redirect:/events";
    }

    // --- FAN REVIEWS ---
    @PostMapping("/events/{id}/review")
    public String submitReview(
            @PathVariable Long id,
            @RequestParam String reviewerName,
            @RequestParam String reviewerEmail,
            @RequestParam Integer rating,
            @RequestParam String reviewText) {
        
        com.example.RoyaltyManager.model.Event event = royaltyService.getEventById(id);
        if (event != null) {
            com.example.RoyaltyManager.model.EventReview review =
                    new com.example.RoyaltyManager.model.EventReview(event, reviewerName, reviewerEmail, rating, reviewText);
            royaltyService.saveReview(review);
        }
        return "redirect:/events";
    }
}
