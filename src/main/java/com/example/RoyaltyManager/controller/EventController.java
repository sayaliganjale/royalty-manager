package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.Event;
import com.example.RoyaltyManager.service.RoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Controller
public class EventController {

    @Autowired
    private RoyaltyService royaltyService;

    // Public Pages
    @GetMapping("/events")
    public String publicEventsPage(Model model) {
        model.addAttribute("events", royaltyService.getAllEvents());
        model.addAttribute("today", LocalDate.now());
        return "public_events";
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
            @RequestParam Double amount) {
        
        System.out.println("Processing ticket request for event: " + id + " | Buyer: " + buyerName);
        royaltyService.purchaseTicket(id, buyerName, buyerEmail, quantity, ticketType, amount);
        System.out.println("Purchase successful for: " + buyerEmail);
        
        return "redirect:/events?success=true";
    }

    // Admin Pages
    @GetMapping("/admin/events")
    public String manageEventsPage(Model model) {
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
            @RequestParam Long artistId) {
        
        List<Artist> artists = royaltyService.getAllArtists();
        Artist selectedArtist = artists.stream().filter(a -> a.getId().equals(artistId)).findFirst().orElse(null);
        
        if (selectedArtist != null) {
            Event event = new Event(name, eventDate, venue, ticketPrice, selectedArtist);
            royaltyService.saveEvent(event);
        }
        
        return "redirect:/admin/events";
    }
}
