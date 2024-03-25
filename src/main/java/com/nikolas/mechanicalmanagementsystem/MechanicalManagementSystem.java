package com.nikolas.mechanicalmanagementsystem;

import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.entity.UserRoleEnum;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
@EnableCaching
public class MechanicalManagementSystem implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MechanicalManagementSystem.class, args);
        log.info("MechanicalManagementSystem::main - APPLICATION STARTED...");

    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationContext applicationContext;


    @Override
    public void run(String... args) {
        if (Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()).isEmpty()) {
            if (!userRepository.existsByEmail("admin@gmail.com")) {
                LocalDateTime registrationDate = LocalDateTime.now();

                User userAdmin = User.builder()
                        .firstName("ADMIN")
                        .lastName("ADMIN")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin"))
                        .isEnabled(true)
                        .isAccountNonLocked(true)
                        .roles(Collections.singleton(new Role(UserRoleEnum.ADMIN.name())))
                        .registrationDate(registrationDate)
                        .build();
                userRepository.save(userAdmin);

            }
        }
    }
}
