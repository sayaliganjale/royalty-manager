package com.example.RoyaltyManager.service;

import org.springframework.stereotype.Service;

@Service
public class MockEmailService {

    public void sendEmail(String to, String subject, String body) {
        System.out.println("======================================================");
        System.out.println("📧 MOCK EMAIL NOTIFICATION TRIGGERED");
        System.out.println("======================================================");
        System.out.println("TO      : " + to);
        System.out.println("SUBJECT : " + subject);
        System.out.println("------------------------------------------------------");
        System.out.println(body);
        System.out.println("======================================================");
    }
}
