package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.model.*;
import com.example.RoyaltyManager.service.RoyaltyService;
import com.example.RoyaltyManager.repository.DisputeRepository; // Naya Import
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
public class RoyaltyController {

    @Autowired
    private RoyaltyService royaltyService;

    @Autowired
    private DisputeRepository disputeRepo; // Dispute access karne ke liye

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("Accessing Dashboard - adminSession: " + session.getAttribute("adminSession"));
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        // Stats for cards
        model.addAttribute("totalArtists", royaltyService.getTotalArtistsCount());
        model.addAttribute("totalTracks", royaltyService.getTotalTracksCount());
        model.addAttribute("totalRevenue", royaltyService.getTotalRevenue());
        model.addAttribute("totalEventRevenue", royaltyService.getTotalEventRevenue());
        model.addAttribute("ticketPurchases", royaltyService.getAllTicketPurchases());
        
        // Table data (Transactions)
        model.addAttribute("transactions", royaltyService.getAllTransactions());
        
        // RESTORED: Artist Payout Data
        model.addAttribute("artistPayouts", royaltyService.getArtistPayouts());

        // Artist data (For the restored Payouts section)
        model.addAttribute("artists", royaltyService.getAllArtists());

        // Dispute data (Noah Rhodes ki complaints)
        model.addAttribute("disputes", disputeRepo.findAll()); 
        
        return "dashboard";
    }

    // NAYA METHOD: Dispute ko solve karne ke liye
    @PostMapping("/resolve-dispute/{id}")
    public String resolveDispute(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (id == null) return "redirect:/dashboard";
        disputeRepo.findById(id).ifPresent(d -> {
            d.setStatus("RESOLVED");
            disputeRepo.save(d);
        });
        return "redirect:/dashboard";
    }

    @GetMapping("/add-artist")
    public String showArtistForm(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("artist", new Artist());
        return "artist_form";
    }

    @PostMapping("/save-artist")
    public String saveArtist(@ModelAttribute Artist artist, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        royaltyService.saveArtist(artist);
        return "redirect:/dashboard";
    }

    @GetMapping("/label/track-registration")
    public String showTrackForm(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("track", new Track());
        model.addAttribute("artists", royaltyService.getAllArtists());
        return "track_registration";
    }

    @PostMapping("/label/save-track")
    public String saveTrack(@ModelAttribute Track track, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        royaltyService.saveTrack(track);
        return "redirect:/dashboard";
    }

    @GetMapping("/label/contract-management")
    public String showContractForm(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("contract", new Contract());
        model.addAttribute("artists", royaltyService.getAllArtists());
        return "contract_form";
    }

    @GetMapping("/label/analytics")
    public String showAnalytics(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        List<RoyaltyTransaction> transactions = royaltyService.getAllTransactions();
        double totalGross = royaltyService.getTotalRevenue(); 
        
        double artistTotal = totalGross * 0.70;
        double labelTotal = totalGross * 0.30;
        
        model.addAttribute("artistTotal", artistTotal);
        model.addAttribute("labelTotal", labelTotal);
        model.addAttribute("totalRevenue", totalGross);
        
        model.addAttribute("transactions", transactions.size() > 10 ? transactions.subList(0, 10) : transactions);
        
        return "analytics_report";
    }

    @PostMapping("/upload-csv")
    public String uploadCSV(@RequestParam("file") MultipartFile file, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        try {
            royaltyService.processCSV(file);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return "redirect:/dashboard";
    }
}