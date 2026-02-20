package com.busbooking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BusBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusBookingApplication.class, args);
    }

    // ✅ App start hone ke baad clear localhost message
    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println("======================================");
            System.out.println(" Bus Booking App Started Successfully");
            System.out.println(" Open in browser: http://localhost:8081/");
            System.out.println("======================================");
        };
    }
}