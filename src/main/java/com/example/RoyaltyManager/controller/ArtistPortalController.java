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
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequestMapping("/artist")
public class ArtistPortalController {

    @Autowired
    private RoyaltyService royaltyService;
    

    @GetMapping("/dashboard")
    public String artistDashboard(HttpSession session, Model model) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";

        List<RoyaltyTransaction> allTx = royaltyService.getAllTransactions();
        List<RoyaltyTransaction> myTx = allTx.stream()
                .filter(t -> t.getArtist() != null && t.getArtist().getId().equals(artist.getId()))
                .collect(Collectors.toList());

        double myEarnings = myTx.stream().mapToDouble(t -> t.getGrossRevenue() * (artist.getContractSplit() / 100.0)).sum();
        double myEventRevenueTotal = royaltyService.getEventRevenueForArtist(artist.getId());
        double myEventEarnings = myEventRevenueTotal * (artist.getContractSplit() / 100.0);

        List<com.example.RoyaltyManager.model.TicketPurchase> myPurchases = royaltyService.getTicketPurchasesByArtist(artist.getId());
        int totalFans = myPurchases.stream().mapToInt(com.example.RoyaltyManager.model.TicketPurchase::getQuantity).sum();

        Map<String, Double> platformEarnings = myTx.stream()
            .collect(Collectors.groupingBy(RoyaltyTransaction::getPlatform,
                     Collectors.summingDouble(t -> t.getGrossRevenue() * (artist.getContractSplit() / 100.0))));
        
        double totalRevenue = myEarnings + myEventEarnings;
        String badgeLevel = totalRevenue > 10000 ? "Diamond 💎" : (totalRevenue > 5000 ? "Platinum 💿" : (totalRevenue > 1000 ? "Gold 🏅" : "Silver 🥈"));
        
        String bestPlatform = "None";
        if (!platformEarnings.isEmpty()) {
            bestPlatform = Collections.max(platformEarnings.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        String smartInsight = "Focus your marketing on " + bestPlatform + ". You are generating the most revenue there!";

        // Top Songs Leaderboard
        java.util.List<Map<String, Object>> topSongs = royaltyService.getTopSongsByRevenue(artist.getId(), 5);

        // Disputes
        java.util.List<com.example.RoyaltyManager.model.Dispute> myDisputes = royaltyService.getDisputesByArtist(artist.getId());

        // Payout Requests
        java.util.List<com.example.RoyaltyManager.model.PayoutRequest> myPayouts = royaltyService.getPayoutRequestsByArtist(artist.getId());

        model.addAttribute("artist", artist);
        model.addAttribute("myTransactions", myTx);
        model.addAttribute("myEarnings", myEarnings);
        model.addAttribute("myEventEarnings", myEventEarnings);
        model.addAttribute("myTicketPurchases", myPurchases);
        model.addAttribute("totalFans", totalFans);
        model.addAttribute("platformEarnings", platformEarnings);
        model.addAttribute("badgeLevel", badgeLevel);
        model.addAttribute("smartInsight", smartInsight);
        model.addAttribute("topSongs", topSongs);
        model.addAttribute("myDisputes", myDisputes);
        model.addAttribute("myPayouts", myPayouts);
        model.addAttribute("totalEarnings", totalRevenue);
        
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

    // 4. Raise Dispute
    @PostMapping("/raise-dispute")
    public String raiseDispute(@RequestParam String trackName, @RequestParam String reason, HttpSession session) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";

        Dispute dispute = new Dispute();
        dispute.setArtist(artist);
        dispute.setTrackName(trackName);
        dispute.setReason(reason);
        royaltyService.saveDispute(dispute);
        return "redirect:/artist/dashboard?tab=disputes";
    }

    // 5. Submit Payout Request
    @PostMapping("/request-payout")
    public String requestPayout(
            @RequestParam Double amount,
            @RequestParam String bankDetails,
            HttpSession session) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return "redirect:/artist/login";
        com.example.RoyaltyManager.model.PayoutRequest req =
                new com.example.RoyaltyManager.model.PayoutRequest(artist, amount, bankDetails);
        royaltyService.savePayoutRequest(req);
        return "redirect:/artist/dashboard?tab=payouts";
    }

    @GetMapping("/download-invoice")
    public ResponseEntity<byte[]> downloadInvoice(HttpSession session) {
        Artist artist = (Artist) session.getAttribute("loggedInArtist");
        if (artist == null) return ResponseEntity.status(401).build();

        List<RoyaltyTransaction> myTx = royaltyService.getAllTransactions().stream()
                .filter(t -> t.getArtist() != null && t.getArtist().getId().equals(artist.getId()))
                .collect(Collectors.toList());
        double myEarnings = myTx.stream().mapToDouble(t -> t.getGrossRevenue() * (artist.getContractSplit() / 100.0)).sum();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            document.add(new Paragraph("=========================================================="));
            document.add(new Paragraph("               OFFICIAL ROYALTY INVOICE                   "));
            document.add(new Paragraph("=========================================================="));
            document.add(new Paragraph("\nArtist Name : " + artist.getName()));
            document.add(new Paragraph("Email       : " + artist.getEmail()));
            document.add(new Paragraph("Split       : " + artist.getContractSplit() + "%\n"));
            document.add(new Paragraph("----------------------------------------------------------"));
            document.add(new Paragraph("Total Streams Tracked : " + myTx.size() + " records tracked."));
            document.add(new Paragraph("TOTAL STREAM REVENUE  : $" + String.format("%.2f", myEarnings)));
            document.add(new Paragraph("----------------------------------------------------------\n"));
            document.add(new Paragraph("=========================================================="));
            document.add(new Paragraph("* This document is auto-generated by EliteStudio Manager *"));
            
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Invoice_" + artist.getName().replace(" ", "_") + ".pdf");
            
            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
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