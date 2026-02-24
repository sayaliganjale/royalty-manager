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

    // Save Logic
    public void saveArtist(Artist artist) { artistRepo.save(artist); }
    public void saveTrack(Track track) { trackRepo.save(track); }
    public void saveContract(Contract contract) { contractRepo.save(contract); }

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
}