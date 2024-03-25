package com.nikolas.mechanicalmanagementsystem.registration.emailVerification;

import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenService;

import java.util.Optional;

public interface EmailVerificationService extends BaseTokenService {
    String checkAndValidateRegistration(String token);
    Optional<EmailVerificationToken> findByToken(String token);
}
