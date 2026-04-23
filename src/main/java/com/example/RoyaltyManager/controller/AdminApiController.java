package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.RoyaltyTransaction;
import com.example.RoyaltyManager.service.RoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private RoyaltyService royaltyService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLiveStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            double totalRev = royaltyService.getAllTransactions().stream()
                .mapToDouble(RoyaltyTransaction::getGrossRevenue).sum();
            stats.put("totalRevenue", totalRev);
            stats.put("totalDisputes", royaltyService.getAllDisputes().size());
            stats.put("totalEventRevenue", royaltyService.getTotalEventRevenue());
            
            // Total system share
            double systemShare = (totalRev + royaltyService.getTotalEventRevenue()) * 0.30;
            stats.put("systemShare", systemShare);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulateTraffic() {
        try {
            List<Artist> artists = royaltyService.getAllArtists();
            if (artists.isEmpty()) return ResponseEntity.ok("No artists to simulate");
            
            Random random = new Random();
            String[] platforms = {"Spotify", "Apple Music", "YouTube", "Tidal", "Amazon Music"};
            String[] tracks = {"Midnight Melodies", "Neon Lights", "Summer Breeze", "Lost in Echoes", "Acoustic Session"};
            
            for (int i = 0; i < 5; i++) {
                Artist randomArtist = artists.get(random.nextInt(artists.size()));
                RoyaltyTransaction tx = new RoyaltyTransaction();
                tx.setArtist(randomArtist);
                tx.setPlatformArtistName(randomArtist.getName());
                tx.setSongTitle(tracks[random.nextInt(tracks.length)]);
                tx.setPlatform(platforms[random.nextInt(platforms.length)]);
                tx.setStreamCount(1000 + random.nextInt(9000));
                tx.calculateRevenue();
                tx.setReportedPayout(tx.getCalculatedExpected() * 0.95); // 5% discrepancy randomly maybe
                royaltyService.saveTransaction(tx);
            }
            return ResponseEntity.ok("Simulated 5 streams successfully");
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error");
        }
    }
}
