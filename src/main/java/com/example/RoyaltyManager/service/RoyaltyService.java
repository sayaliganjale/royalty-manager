package com.example.RoyaltyManager.service;

import com.example.RoyaltyManager.model.*;
import com.example.RoyaltyManager.repository.*;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
public class RoyaltyService {

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private ArtistRepository artistRepo;
    @Autowired private TrackRepository trackRepo;
    @Autowired private ContractRepository contractRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private TicketPurchaseRepository ticketRepo;

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
    public List<Event> getAllEvents() { return eventRepo.findAll(); }
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
            // Re-calculate the price on backend for security
            double multiplier = 1.0;
            if ("VIP Access".equalsIgnoreCase(ticketType)) multiplier = 2.0;
            else if ("Meet & Greet".equalsIgnoreCase(ticketType)) multiplier = 3.5;
            
            double calculatedAmount = event.getTicketPrice() * quantity * multiplier;
            
            TicketPurchase purchase = new TicketPurchase(event, buyerName, buyerEmail, quantity, calculatedAmount, ticketType);
            ticketRepo.save(purchase);
        }
    }

    public List<TicketPurchase> getAllTicketPurchases() { 
        return ticketRepo.findAll(); 
    }

    public List<TicketPurchase> getTicketPurchasesByArtist(Long artistId) {
        List<Event> artistEvents = eventRepo.findByArtistId(artistId);
        List<Long> eventIds = artistEvents.stream().map(Event::getId).toList();
        return ticketRepo.findAll().stream()
                .filter(p -> eventIds.contains(p.getEvent().getId()))
                .toList();
    }

    public double getTotalEventRevenue() {
        return ticketRepo.findAll().stream()
                .mapToDouble(TicketPurchase::getTotalAmount)
                .sum();
    }

    public double getEventRevenueForArtist(Long artistId) {
        List<Event> artistEvents = eventRepo.findByArtistId(artistId);
        double totalRevenue = 0.0;
        for(Event event : artistEvents) {
            List<TicketPurchase> purchases = ticketRepo.findByEventId(event.getId());
            double eventRevenue = purchases.stream().mapToDouble(TicketPurchase::getTotalAmount).sum();
            totalRevenue += eventRevenue;
        }
        return totalRevenue;
    }

    // Save Logic
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

    // CSV Magic
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