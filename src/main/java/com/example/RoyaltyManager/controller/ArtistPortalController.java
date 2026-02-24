package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.model.Artist;
import com.example.RoyaltyManager.model.Dispute;
import com.example.RoyaltyManager.model.RoyaltyTransaction;
import com.example.RoyaltyManager.service.RoyaltyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/artist")
public class ArtistPortalController {

    @Autowired
    private RoyaltyService royaltyService;
    
    @Autowired
    private com.example.RoyaltyManager.repository.DisputeRepository disputeRepo;
    
    // 1. Dashboard (Point 2)
    @GetMapping("/dashboard")
    public String artistDashboard(HttpSession session, Model model) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";

        List<RoyaltyTransaction> allTx = royaltyService.getAllTransactions();
        List<RoyaltyTransaction> myTx = allTx.stream()
                .filter(t -> t.getArtist() != null && t.getArtist().getId().equals(artist.getId()))
                .collect(Collectors.toList());

        double myEarnings = myTx.stream().mapToDouble(t -> t.getGrossRevenue() * 0.70).sum();

        model.addAttribute("artist", artist);
        model.addAttribute("myTransactions", myTx);
        model.addAttribute("myEarnings", myEarnings);
        
        return "artist_dashboard";
    }

    // 2. Profile & Bank Details (Point 10) - FIX LAGAYA HAI YAHAN
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";
        
        model.addAttribute("artist", artist);
        return "artist_profile"; 
    }

    // 3. Payment History (Point 7) - FIX LAGAYA HAI YAHAN
    @GetMapping("/payments")
    public String getPaymentHistory(HttpSession session) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";
        
        return "payment_history";
    }

    // 4. Raise Dispute (Point 8)
    @PostMapping("/raise-dispute")
    public String raiseDispute(@RequestParam String trackName, @RequestParam String reason, HttpSession session) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";

        Dispute dispute = new Dispute();
        dispute.setArtist(artist);
        dispute.setTrackName(trackName);
        dispute.setReason(reason);
        
        disputeRepo.save(dispute);
        return "redirect:/artist/dashboard?success=dispute_raised";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "artist_login";
    }

    @PostMapping("/do-login")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        List<Artist> artists = royaltyService.getAllArtists();
        Artist loggedInArtist = artists.stream()
                .filter(a -> email.equalsIgnoreCase(a.getEmail()) && password.equals(a.getPassword()))
                .findFirst()
                .orElse(null);

        if (loggedInArtist != null) {
            session.setAttribute("loggedInArtist", loggedInArtist);
            return "redirect:/artist/dashboard";
        }
        return "redirect:/artist/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/artist/login";
    }
}