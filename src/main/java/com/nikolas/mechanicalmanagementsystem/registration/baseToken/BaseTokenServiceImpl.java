package com.nikolas.mechanicalmanagementsystem.registration.baseToken;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import com.nikolas.mechanicalmanagementsystem.utility.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract base class for token service implementations.
 *
 * <p>This class provides a foundation for implementing token-based functionalities.
 * It uses generics to work with various token types and their corresponding repositories.
 *
 * @param <T> the type of token handled by the service, must extend {@link BaseToken}
 * @param <R> the type of repository used to access token data, must extend {@link BaseTokenRepository}
 */

@Slf4j
public abstract class BaseTokenServiceImpl<T extends BaseToken, R extends BaseTokenRepository<T>> implements BaseTokenService {

    protected EmailSender eventListener;
    protected PasswordEncoder passwordEncoder;
    protected UserRepository userRepository;
    protected R repository;

    /**
     * Constructs a new instance of BaseTokenServiceImpl with the specified repository.
     *
     * @param repository the repository for handling token related data access
     */
    protected BaseTokenServiceImpl(R repository, PasswordEncoder passwordEncoder, UserRepository userRepository,
                                   EmailSender eventListener) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.eventListener = eventListener;
    }

    /**
     * Validates the expiration and validity of a token.
     *
     * @param theToken The token to validate.
     * @return A string indicating the validity status of the token:
     * - "INVALID" if the token is invalid or not found.
     * - "EXPIRED" if the token has expired.
     * - "VALID" if the token is valid and not expired.
     */
    @Override
    public String checkTokenValidity(String theToken) {
        // Search for the token in the repository
        Optional<T> optionalToken = repository.findByToken(theToken);
        T token = null;
        // If the token is found, load it
        if (optionalToken.isPresent()) {
            token = optionalToken.get();
        }
        // If the token is null or not valid, return "INVALID"
        if (token == null || !token.isValid()) {
            log.info("BaseTokenServiceImpl::checkTokenValidity - token is INVALID: {}", theToken);
            return "INVALID";
        }
        // Check the expiration time
        LocalDateTime expirationTime = token.getRequestTime();
        log.info("BaseTokenServiceImpl::checkTokenValidity - ExpTime: " + expirationTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        log.info("BaseTokenServiceImpl::checkTokenValidity - Time now: " + formattedNow);

        // If the current time is after the expiration time, the token is expired
        if (now.isAfter(expirationTime)) {
            log.info("BaseTokenServiceImpl::checkTokenValidity - token is EXPIRED: {}", theToken);
            return "EXPIRED";
        }
        // Set the token validity to false and save it to the database
        token.setValid(false);
        log.info("BaseTokenServiceImpl::checkTokenValidity - Token set from VALID to INVALID");
        repository.save(token);
        // Return "VALID"
        return "VALID";
    }


    /**
     * Retrieves the user associated with the given token from the database based on the provided token and token repository.
     *
     * @param theToken        The token to search for.
     * @param tokenRepository The repository responsible for storing tokens.
     * @return An optional value containing the user if the token is found, or empty if not found.
     */
    @Override
    public Optional<User> findUserByToken(String theToken, BaseTokenRepository<? extends BaseToken> tokenRepository) {
        // Search for the token in the repository
        Optional<? extends BaseToken> tokenOptional = tokenRepository.findByToken(theToken);
        // If the token is found, retrieve the associated user
        if (tokenOptional.isPresent()) {
            BaseToken baseToken = tokenOptional.get();
            User user = baseToken.getUser();
            log.info("BaseTokenServiceImpl::findUserByToken - TOKEN: {}, USER: {}", theToken, user.getFirstName() + ' ' + user.getLastName());
            return Optional.of(user);
        } else {
            // If the token is not found, return an empty optional
            log.error("BaseTokenServiceImpl::findUserByToken - Optional.empty");
            return Optional.empty();
        }
    }

    /**
     * Changes the password for a given user.
     *
     * @param user        the user whose password will be changed
     * @param newPassword the new password to set
     */
    @Override
    public void changePassword(User user, String newPassword) {
        // Encode the new password and update the user's password field
        user.setPassword(passwordEncoder.encode(newPassword));
        // Save the updated user object to the database
        userRepository.save(user);
    }

    /**
     * Creates a token for the given user and saves it to the database.
     *
     * @param user  the user for whom the token will be created
     * @param token the token string to associate with the user
     */
    @Override
    public void createTokenForUser(User user, String token) {
        // Create a token instance for the user
        T generatedToken = createTokenInstance(user, token);
        // Save the generated token to the database
        getTokenRepository().save(generatedToken);
        log.info("BaseTokenServiceImpl::createTokenForUser - token generated");
    }


    /**
     * Deletes all tokens associated with a user specified by their ID.
     *
     * @param userId the ID of the user whose tokens will be deleted
     */
    @Override
    public void deleteTokensByUserId(Long userId) {
        // Find all tokens associated with the specified user ID
        List<T> tokens = repository.findByUserId(userId);
        // Delete all found tokens from the database
        repository.deleteAll(tokens);
    }

    /**
     * Processes the user's request based on the provided email and request details,
     * generates a unique token, and sends an email notification accordingly.
     * The method determines the type of request (e.g., email verification, password change, etc.)
     * and handles the token generation and email sending accordingly.
     *
     * @param email   The email address of the user making the request.
     * @param request The HttpServletRequest object containing request details.
     * @return A String indicating the outcome of the request processing (e.g., success, error).
     */
    @Override
    public String processRequestAndSendEmail(String email, HttpServletRequest request) {
        // Get the current authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("BaseTokenServiceImpl::processRequestAndSendEmail - authentication: {}", authentication.getName());

        // Get the application URL and servlet path
        String url = UrlUtil.getApplicationUrl(request);
        log.info("BaseTokenServiceImpl::processRequestAndSendEmail - getApplicationUrl: {}", url);
        String servletPath = request.getRequestURI();
        log.info("BaseTokenServiceImpl::processRequestAndSendEmail - getRequestURI: {}", servletPath);

        // Find the user by email address
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Return "email_not_found" if the user is not found
        if (userOptional.isEmpty()) {
            return "email_not_found";
        }

        // Generate a token for the user
        String token = UUID.randomUUID().toString();
        User user = userOptional.get();
//        log.info("BaseTokenServiceImpl::processRequestAndSendEmail - send email to: {}", user.getEmail());

        //Resend verification token to email
        if (servletPath.equals("/system/registration/sendVerificationToken")) {
            // Check if the user is already verified
            if (user.isEnabled()) {
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - Email already verified: {}", user.getEmail());
                return "email_verified";
            }
            // Create and send the verification email
            createTokenForUser(user, token);
            url = url + "/registration/verifyEmail?token=" + token;
            try {
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - Resend ver. email URL: {}", url);
                eventListener.sendNewVerificationEmail(user, url);
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - send email to: {}", user.getEmail());
                return "success";
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.error("BaseTokenServiceImpl::processRequestAndSendEmail - something went wrong while requesting the send email verification request: {}", e.getMessage());
                return "error";
            }
        }

        //Change password in profile
        if (!authentication.getName().equals("anonymousUser") && authentication.isAuthenticated() && servletPath.equals("/system/changePassword")) {
            // Create and send the change password email
            createTokenForUser(user, token);
            url = url + "/forgotPassword/change-password?token=" + token;
            try {
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - Change PW URL: {}", url);
                eventListener.sendChangePasswordRequestEmail(user, url);
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - send email to: {}", user.getEmail());
                return "success";
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.error("BaseTokenServiceImpl::processRequestAndSendEmail - something went wrong while requesting the password change: {}", e.getMessage());
                return "error";
            }

            //Forgotten password on the home page
        } else if (servletPath.equals("/system/forgotPassword/send-email")) {
            // Check if the user's account is locked
            if (!user.isAccountNonLocked()) {
                return "account_locked";
            }
            // Create and send the forgotten password email
            createTokenForUser(user, token);
            url = url + "/forgotPassword/forgot-password?token=" + token;
            try {
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - Forgotten PW URL: {}", url);
                eventListener.sendForgotPasswordRequestEmail(user, url);
                log.info("BaseTokenServiceImpl::processRequestAndSendEmail - send email to: {}", user.getEmail());
                return "success";
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.error("BaseTokenServiceImpl::processRequestAndSendEmail - something went wrong while requesting the password change: {}", e.getMessage());
                return "error";
            }
        }
        // Return null if no action is taken
        return null;
    }


    /**
     * Processes password change request with token validation.
     * Validates the provided token and ensures its validity before proceeding
     * with the password change operation for the associated user.
     * If the token is valid and the user is found, it verifies the old password,
     * changes it to the new one, sends a success email notification, and logs out the user.
     * Handles various error scenarios like invalid or expired token,
     * incorrect old password, or email sending failure.
     *
     * @param theToken        The token associated with the password change request.
     * @param oldPassword     The current password of the user.
     * @param newPassword     The new password requested by the user.
     * @param confirmPassword The confirmation of the new password.
     * @param request         The HTTP request object.
     * @param response        The HTTP response object -> it can be left rn.
     * @return A string indicating the result of the password change operation
     * (e.g., "pw_error", "empty_user", "invalid_pw_token", "expired_pw_token", etc.).
     */
    @Override
    @Transactional
    public String processPasswordChangeWithTokenValidation(String theToken, String oldPassword, String newPassword, String confirmPassword,
                                                           HttpServletRequest request, HttpServletResponse response) {
        // Get the token repository
        BaseTokenRepository<? extends BaseToken> tokenRepository = (BaseTokenRepository<? extends BaseToken>) getTokenRepository();

        // Find the user associated with the token
        Optional<User> optionalUser = findUserByToken(theToken, tokenRepository);

        // If the user is found, validate the password change
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!validatePasswordChange(user, oldPassword, newPassword, confirmPassword)) {
                return "pw_error";
            }
        } else {
            return "empty_user";
        }

        // Check the validity of the token
        String tokenValidityResult = checkTokenValidity(theToken);

        // Handle different token validity scenarios
        if (tokenValidityResult.equalsIgnoreCase("invalid")) {
            return "invalid_pw_token";
        }
        if (!tokenValidityResult.equalsIgnoreCase("valid")) {
            return "expired_pw_token";
        }

        try {
            // Change the user's password, send email notification, and logout the user
            changePassword(optionalUser.get(), newPassword);
            eventListener.sendPasswordSuccessfullyChangedEmail(optionalUser.get());
            logoutUser(request, response);
            log.info("BaseTokenServiceImpl::processPasswordChangeWithTokenValidation - User {} successfully changed pw", optionalUser.get().getEmail());
            return "change_pw_success";
        } catch (MessagingException | UnsupportedEncodingException e) {
            return "email_send_error";
        }
    }

    // Inherited classes implement these methods

    /**
     * Creates an instance of the token for the given user and token string.
     *
     * @param user  the user for whom the token will be created
     * @param token the token string to associate with the user
     * @return an instance of the token
     */
    protected abstract T createTokenInstance(User user, String token);

    /**
     * Retrieves the repository used to access token data.
     *
     * @return the repository for token data access
     */
    protected abstract JpaRepository<T, Long> getTokenRepository();

    /**
     * Validates a password change operation for a user.
     *
     * @return {@code true} if the password change is valid, {@code false} otherwise
     */
    protected abstract boolean validatePasswordChange(User user, String oldPassword, String newPassword, String confirmPassword);

    /**
     * Logs out the user associated with the given request and response.
     */
    protected abstract void logoutUser(HttpServletRequest request, HttpServletResponse response);

}

