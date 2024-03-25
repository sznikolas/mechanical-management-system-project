package com.nikolas.mechanicalmanagementsystem.registration.emailVerification;

import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends BaseTokenRepository<EmailVerificationToken> {
    List<EmailVerificationToken> findByUserId(Long userId);
    Optional<EmailVerificationToken> findByToken(String token);
}
