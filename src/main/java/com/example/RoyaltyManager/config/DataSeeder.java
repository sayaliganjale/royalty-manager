package com.example.RoyaltyManager.config;

import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.Event;
import com.example.RoyaltyManager.model.TicketPurchase;
import com.example.RoyaltyManager.repository.ArtistRepository;
import com.example.RoyaltyManager.repository.EventRepository;
import com.example.RoyaltyManager.repository.TicketPurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketPurchaseRepository ticketPurchaseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Clear existing data to ensure a clean demo state (as requested)
        ticketPurchaseRepository.deleteAll();
        eventRepository.deleteAll();

        // Ensure "Noah Rodes" exists
        Artist noah = artistRepository.findByName("Noah Rodes").orElseGet(() -> {
            Artist newArtist = new Artist("Noah Rodes", "noah.rodes@elite.com", "pass123", 75.0);
            return artistRepository.save(newArtist);
        });

        // 1. Available Event for Noah Rodes (The Future Tour)
        Event noahAvailable1 = new Event(
                "Noah Rodes - The Future Tour (London)",
                LocalDate.now().plusDays(30),
                "O2 Arena",
                150.0,
                500, // Total Capacity
                noah
        );
        eventRepository.save(noahAvailable1);

        // 2. Another Available Event for Noah Rodes
        Event noahAvailable2 = new Event(
                "Noah Rodes - The Future Tour (Paris)",
                LocalDate.now().plusDays(45),
                "Accor Arena",
                160.0,
                500, // Total Capacity
                noah
        );
        eventRepository.save(noahAvailable2);

        // 3. Demo Event: Exactly 4 tickets available
        Event demoEvent = new Event(
                "Noah Rodes - Exclusive Demo Night",
                LocalDate.now().plusDays(10),
                "The Lab",
                200.0,
                100, // Total Capacity
                noah
        );
        eventRepository.save(demoEvent);
        // Create 96 ticket purchases to leave exactly 4
        TicketPurchase tpdemo = new TicketPurchase(demoEvent, "Demo Buyer", "demo@student.com", 96, 19200.0, "General Admission");
        ticketPurchaseRepository.save(tpdemo);

        // 4. Seed 2-3 other artists as "Sold Out"
        List<Artist> artists = artistRepository.findAll();
        int otherArtistsCount = 0;
        for (Artist artist : artists) {
            if (artist.getName().equals("Noah Rodes")) continue;
            if (otherArtistsCount >= 3) break;

            Event soldOutEvent = new Event(
                    artist.getName() + " - Sold Out Tour",
                    LocalDate.now().plusDays(60),
                    "Stadium " + (otherArtistsCount + 1),
                    120.0,
                    100, // Total Capacity
                    artist
            );
            eventRepository.save(soldOutEvent);
            // Create 100 ticket purchases to make it sold out
            TicketPurchase tpsold = new TicketPurchase(soldOutEvent, "Sold Out Buyer", "admin@elite.com", 100, 12000.0, "General Admission");
            ticketPurchaseRepository.save(tpsold);
            
            otherArtistsCount++;
        }

        System.out.println("[DataSeeder] Successfully seeded Demo Data.");
    }
}
