package com.nikolas.mechanicalmanagementsystem.registration.baseToken;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;


public interface BaseTokenService {
    String checkTokenValidity(String theToken);
    Optional<User> findUserByToken(String theToken, BaseTokenRepository<? extends BaseToken> tokenRepository);
    void changePassword(User theUser, String newPassword);
    void createTokenForUser(User user, String token);
    void deleteTokensByUserId(Long userId);
    String processRequestAndSendEmail(String email, HttpServletRequest request);
    String processPasswordChangeWithTokenValidation(String theToken, String oldPassword, String newPassword, String confirmPassword, HttpServletRequest request, HttpServletResponse response);

}
