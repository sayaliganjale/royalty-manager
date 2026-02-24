package com.example.RoyaltyManager.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String email; 
    
    private String password; // Member 3 login ke liye zaroori hai

    private Double contractSplit = 70.0; // Default split 70% for artist

    // Relationship: Ek Artist ke paas bahut saare transactions ho sakte hain
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    private List<RoyaltyTransaction> transactions;

    // --- Constructors ---
    public Artist() {}

    public Artist(String name, String email, String password, Double contractSplit) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contractSplit = contractSplit;
    }

    // --- Getters and Setters ---
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    // Is method ki wajah se error aa raha tha, ab fix ho jayega
    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password; 
    }

    public Double getContractSplit() { 
        return contractSplit; 
    }
    public void setContractSplit(Double contractSplit) { 
        this.contractSplit = contractSplit; 
    }

    public List<RoyaltyTransaction> getTransactions() { 
        return transactions; 
    }
    public void setTransactions(List<RoyaltyTransaction> transactions) { 
        this.transactions = transactions; 
    }
}