package com.example.RoyaltyManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoyaltyManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoyaltyManagerApplication.class, args);
	}

}
