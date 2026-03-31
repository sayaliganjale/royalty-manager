package com.example.RoyaltyManager.config;

import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.Event;
import com.example.RoyaltyManager.repository.ArtistRepository;
import com.example.RoyaltyManager.repository.EventRepository;
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

    @Override
    public void run(String... args) throws Exception {
        // Only run if artists exist and no events have been created yet
        if (artistRepository.count() > 0 && eventRepository.count() == 0) {
            List<Artist> artists = artistRepository.findAll();
            
            // Limit to 7 artists max
            int count = Math.min(artists.size(), 7);
            
            for (int i = 0; i < count; i++) {
                Artist artist = artists.get(i);
                
                // Seed 1 Upcoming Event (2026/2027)
                Event upcoming = new Event(
                        artist.getName() + " - The Future Tour",
                        LocalDate.now().plusDays(45 + (i * 10)),
                        "Arena " + (i + 1),
                        150.0 + (i * 10),
                        artist
                );
                eventRepository.save(upcoming);
                
                // Seed 1 Past Event (2024/2025)
                Event past = new Event(
                        artist.getName() + " - Origins '25",
                        LocalDate.now().minusDays(150 + (i * 20)),
                        "Classic Stadium " + (i + 1),
                        90.0 + (i * 5),
                        artist
                );
                eventRepository.save(past);
            }
            
            System.out.println("[DataSeeder] Successfully seeded Events for " + count + " artists (Upcoming & Past).");
        }
    }
}
