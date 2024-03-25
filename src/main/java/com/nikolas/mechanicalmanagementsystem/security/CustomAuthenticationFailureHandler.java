package com.nikolas.mechanicalmanagementsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Handles authentication failure events by redirecting the user to appropriate login failure pages based on the exception type.
     *
     * @param request   the HTTP request
     * @param response  the HTTP response
     * @param exception the authentication exception that occurred
     * @throws IOException if an I/O error occurs during the redirect
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // Extract email from the request parameters
        String email = request.getParameter("username");

        // Handle different types of authentication failure exceptions
        if (exception instanceof BadCredentialsException) {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: {}, Bad credentials: {}", exception.getMessage(), email);
            response.sendRedirect("/system/login?bad_credentials");
        }
        else if (exception instanceof DisabledException) {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: {}, E-mail not verified: {}", exception.getMessage(), email);
            response.sendRedirect("/system/login?email_not_verified");
        }
        else if (exception instanceof LockedException) {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: {}, Account is banned with email: {}", exception.getMessage(), email);
            response.sendRedirect("/system/login?account_locked");
        }
        else if (exception instanceof UsernameNotFoundException) {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: User not found with email: {}", email);
            response.sendRedirect("/system/login?userNotFound");
        }
        else if (exception instanceof SessionAuthenticationException) {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: {} User email: {}", exception.getMessage(), email);
            response.sendRedirect("/system/login?maximum_session");
        }
        else {
            log.error("CustomAuthenticationFailureHandler::onAuthenticationFailure - Unsuccessful login: {}, E-mail: {}", exception.getMessage(), email);
            response.sendRedirect("/system/login?error");
        }
    }

}