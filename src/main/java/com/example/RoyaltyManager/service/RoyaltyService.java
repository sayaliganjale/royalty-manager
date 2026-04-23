package com.example.RoyaltyManager.service;

import com.example.RoyaltyManager.model.*;
import com.example.RoyaltyManager.repository.*;
import com.example.RoyaltyManager.dto.EventDTO;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
public class RoyaltyService {

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private ArtistRepository artistRepo;
    @Autowired private TrackRepository trackRepo;
    @Autowired private ContractRepository contractRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private TicketPurchaseRepository ticketRepo;
    @Autowired private DisputeRepository disputeRepo;
    @Autowired private EventReviewRepository reviewRepo;
    @Autowired private ArtistFollowerRepository followerRepo;
    @Autowired
    private PayoutRequestRepository payoutRepo;

    public List<EventDTO> getPublicEventDTOs() {
        List<Event> events = eventRepo.findAllWithArtist();
        List<Object[]> stats = ticketRepo.countTicketsSoldForEvents();
        Map<Long, Integer> salesMap = new HashMap<>();
        if (stats != null) {
            for (Object[] row : stats) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    salesMap.put((Long) row[0], row[1] != null ? ((Number) row[1]).intValue() : 0);
                }
            }
        }

        return events.stream()
            .filter(e -> e != null && e.getArtist() != null)
            .map(e -> {
                EventDTO dto = new EventDTO();
                dto.setId(e.getId());
                dto.setName(e.getName());
                dto.setEventDate(e.getEventDate());
                dto.setVenue(e.getVenue());
                dto.setTicketPrice(e.getTicketPrice());
                dto.setTotalCapacity(e.getTotalCapacity() != null ? e.getTotalCapacity() : 500);
                
                int sold = salesMap.getOrDefault(e.getId(), 0);
                dto.setTicketsSold(sold);
                dto.setAvailableSeats(Math.max(0, dto.getTotalCapacity() - sold));
                dto.setSoldOut(sold >= dto.getTotalCapacity());
                
                dto.setArtistId(e.getArtist().getId());
                dto.setArtistName(e.getArtist().getName());
                
                dto.setHasReview(e.getReviews() != null && !e.getReviews().isEmpty());
                
                return dto;
            })
            .collect(Collectors.toList());
    }

    // Stats Retrieval
    public List<RoyaltyTransaction> getAllTransactions() { return transactionRepo.findAll(); }
    public List<Artist> getAllArtists() { return artistRepo.findAll(); }
    public long getTotalArtistsCount() { return artistRepo.count(); }
    public long getTotalTracksCount() { return trackRepo.count(); }

    public double getTotalRevenue() {
        return transactionRepo.findAll().stream()
                .mapToDouble(RoyaltyTransaction::getGrossRevenue)
                .sum();
    }

    // Event & Ticket Logic
    public List<Event> getAllEvents() {
        List<Event> events = eventRepo.findAllWithArtist();
        
        // Filter out events with missing or broken artist references to prevent rendering crashes
        List<Event> safeEvents = events.stream()
            .filter(e -> e != null && e.getArtist() != null && e.getArtist().getId() != null)
            .collect(Collectors.toList());

        // Pre-calculate ticket sales for all events in one batch
        List<Object[]> stats = ticketRepo.countTicketsSoldForEvents();
        Map<Long, Integer> salesMap = new HashMap<>();
        if (stats != null) {
            for (Object[] row : stats) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    salesMap.put((Long) row[0], row[1] != null ? ((Number) row[1]).intValue() : 0);
                }
            }
        }
            
        // Assign to transient fields
        safeEvents.forEach(e -> e.setCalculatedTicketsSold(salesMap.getOrDefault(e.getId(), 0)));
        
        return safeEvents;
    }
    public Event getEventById(Long id) {
        if (id == null) return null;
        return eventRepo.findById(id).orElse(null);
    }
    public void saveEvent(Event event) {
        if (event != null) eventRepo.save(event);
    }

    public void purchaseTicket(Long eventId, String buyerName, String buyerEmail, Integer quantity, String ticketType, Double totalAmount) {
        Event event = getEventById(eventId);
        if (event != null) {
            double multiplier = 1.0;
            if ("VIP Access".equalsIgnoreCase(ticketType)) multiplier = 2.0;
            else if ("Meet & Greet".equalsIgnoreCase(ticketType)) multiplier = 3.5;
            double calculatedAmount = event.getTicketPrice() * quantity * multiplier;
            TicketPurchase purchase = new TicketPurchase(event, buyerName, buyerEmail, quantity, calculatedAmount, ticketType);
            ticketRepo.save(purchase);
        }
    }

    public List<TicketPurchase> getAllTicketPurchases() { return ticketRepo.findAll(); }

    public List<TicketPurchase> getTicketPurchasesByArtist(Long artistId) {
        List<Event> artistEvents = eventRepo.findByArtistId(artistId);
        List<Long> eventIds = artistEvents.stream().map(Event::getId).toList();
        return ticketRepo.findAll().stream()
                .filter(p -> eventIds.contains(p.getEvent().getId()))
                .toList();
    }

    public double getTotalEventRevenue() {
        return ticketRepo.findAll().stream().mapToDouble(TicketPurchase::getTotalAmount).sum();
    }

    public double getEventRevenueForArtist(Long artistId) {
        List<Event> artistEvents = eventRepo.findByArtistId(artistId);
        double totalRevenue = 0.0;
        for (Event event : artistEvents) {
            List<TicketPurchase> purchases = ticketRepo.findByEventId(event.getId());
            totalRevenue += purchases.stream().mapToDouble(TicketPurchase::getTotalAmount).sum();
        }
        return totalRevenue;
    }

    // --- DISPUTE METHODS ---
    public List<Dispute> getAllDisputes() { return disputeRepo.findAll(); }
    public List<Dispute> getDisputesByArtist(Long artistId) {
        return disputeRepo.findAll().stream()
                .filter(d -> d.getArtist() != null && d.getArtist().getId().equals(artistId))
                .collect(Collectors.toList());
    }
    public void saveDispute(Dispute dispute) { if (dispute != null) disputeRepo.save(dispute); }
    public void resolveDispute(Long disputeId, String status) {
        disputeRepo.findById(disputeId).ifPresent(d -> { d.setStatus(status); disputeRepo.save(d); });
    }

    // --- PAYOUT REQUEST METHODS ---
    public List<PayoutRequest> getAllPayoutRequests() { return payoutRepo.findAll(); }
    public List<PayoutRequest> getPayoutRequestsByArtist(Long artistId) { return payoutRepo.findByArtistId(artistId); }
    public List<PayoutRequest> getPendingPayouts() { return payoutRepo.findByStatus("PENDING"); }
    public void savePayoutRequest(PayoutRequest req) { if (req != null) payoutRepo.save(req); }
    public void updatePayoutStatus(Long id, String status, String note) {
        payoutRepo.findById(id).ifPresent(r -> { r.setStatus(status); r.setAdminNote(note); payoutRepo.save(r); });
    }

    // --- FAN REVIEW METHODS ---
    public List<EventReview> getReviewsByEvent(Long eventId) { return reviewRepo.findByEventId(eventId); }
    public void saveReview(EventReview review) { if (review != null) reviewRepo.save(review); }

    // --- ARTIST FOLLOWER METHODS ---
    public boolean isFollowing(Long artistId, String email) {
        return followerRepo.existsByArtistIdAndFollowerEmail(artistId, email);
    }
    public void followArtist(Artist artist, String name, String email) {
        if (!followerRepo.existsByArtistIdAndFollowerEmail(artist.getId(), email)) {
            followerRepo.save(new ArtistFollower(artist, name, email));
        }
    }
    public List<ArtistFollower> getFollowersByArtist(Long artistId) { return followerRepo.findByArtistId(artistId); }

    // --- TOP SONGS LEADERBOARD ---
    public List<Map<String, Object>> getTopSongsByRevenue(Long artistId, int limit) {
        List<RoyaltyTransaction> txns = transactionRepo.findAll().stream()
                .filter(t -> t.getArtist() != null && t.getArtist().getId().equals(artistId))
                .collect(Collectors.toList());

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (RoyaltyTransaction t : txns) {
            String song = t.getSongTitle() != null ? t.getSongTitle() : "Unknown";
            revenueMap.merge(song, t.getGrossRevenue(), Double::sum);
        }

        double totalRevenue = revenueMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalRevenue == 0) totalRevenue = 1;
        final double finalTotal = totalRevenue;

        return revenueMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("songTitle", e.getKey());
                    m.put("revenue", e.getValue());
                    m.put("percentage", Math.round((e.getValue() / finalTotal) * 100));
                    return m;
                })
                .collect(Collectors.toList());
    }

    // Save Logic
    public void saveTransaction(RoyaltyTransaction tx) { if (tx != null) transactionRepo.save(tx); }
    public void saveArtist(Artist artist) { if (artist != null) artistRepo.save(artist); }
    public void saveTrack(Track track) { if (track != null) trackRepo.save(track); }
    public void saveContract(Contract contract) { if (contract != null) contractRepo.save(contract); }
    public List<Contract> getAllContracts() { return contractRepo.findAll(); }

    public List<ArtistPayoutDTO> getArtistPayouts() {
        return artistRepo.findAll().stream().map(a -> {
            long tCount = trackRepo.countByArtistId(a.getId());
            double gross = a.getTransactions().stream().mapToDouble(RoyaltyTransaction::getGrossRevenue).sum();
            double net = gross * (a.getContractSplit() / 100.0);
            return new ArtistPayoutDTO(a.getName(), tCount, gross, net, "ACTIVE");
        }).toList();
    }

    @Transactional
    public void processCSV(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<RoyaltyTransaction> csvToBean = new CsvToBeanBuilder<RoyaltyTransaction>(reader)
                    .withType(RoyaltyTransaction.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<RoyaltyTransaction> transactions = csvToBean.parse();
            for (RoyaltyTransaction tx : transactions) {
                tx.calculateRevenue();
                if (tx.getPlatformArtistName() != null && !tx.getPlatformArtistName().isEmpty()) {
                    String aName = tx.getPlatformArtistName().trim();
                    Artist artist = artistRepo.findByName(aName).orElseGet(() -> {
                        Artist newArtist = new Artist();
                        newArtist.setName(aName);
                        newArtist.setEmail(aName.replace(" ", ".").toLowerCase() + "@label.com");
                        newArtist.setContractSplit(50.0);
                        return artistRepo.save(newArtist);
                    });
                    tx.setArtist(artist);
                }
                transactionRepo.save(tx);
            }
        }
    }

    public List<Track> getTracksByArtist(Long artistId) { return trackRepo.findByArtistId(artistId); }
}