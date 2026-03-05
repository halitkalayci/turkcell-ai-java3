package com.turkcell.authservice.config;

import com.turkcell.authservice.entity.Claim;
import com.turkcell.authservice.entity.User;
import com.turkcell.authservice.repository.ClaimRepository;
import com.turkcell.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(ClaimRepository claimRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // D012 — 7 claim seed
            List<String> claimNames = List.of(
                    "Product.Read",
                    "Product.Create",
                    "Product.Update",
                    "Product.Delete",
                    "Order.Read",
                    "Order.Create",
                    "Order.Cancel"
            );

            List<Claim> savedClaims = claimNames.stream()
                    .map(Claim::new)
                    .map(claimRepository::save)
                    .toList();

            // Admin user — tüm claim'lere sahip
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEnabled(true);
            admin.setClaims(new HashSet<>(savedClaims));
            userRepository.save(admin);

            // Read-only user — sadece Read claim'lerine sahip
            Set<Claim> readClaims = savedClaims.stream()
                    .filter(c -> c.getName().endsWith(".Read"))
                    .collect(java.util.stream.Collectors.toSet());

            User reader = new User();
            reader.setUsername("reader");
            reader.setPassword(passwordEncoder.encode("reader"));
            reader.setEnabled(true);
            reader.setClaims(readClaims);
            userRepository.save(reader);
        };
    }
}
