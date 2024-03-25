package com.nikolas.mechanicalmanagementsystem.registration.emailVerification;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenServiceImpl;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
public class EmailVerificationServiceImpl extends BaseTokenServiceImpl<EmailVerificationToken, EmailVerificationRepository> implements EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;

    public EmailVerificationServiceImpl(EmailVerificationRepository emailVerificationRepository, UserRepository userRepository,
                                        EmailSender emailSender, PasswordEncoder passwordEncoder) {
        super(emailVerificationRepository,passwordEncoder, userRepository, emailSender);
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailSender = emailSender;
    }

    /**
     * Creates an instance of the token for email verification.
     *
     * @param user  the user for whom the token will be created
     * @param token the token string to associate with the user
     * @return an instance of {@code EmailVerificationToken} for the user
     */
    @Override
    protected EmailVerificationToken createTokenInstance(User user, String token) {
        return new EmailVerificationToken(token, user);
    }

    /**
     * Retrieves the repository for email verification tokens.
     *
     * @return the repository for {@code EmailVerificationToken}
     */
    @Override
    protected JpaRepository<EmailVerificationToken, Long> getTokenRepository() {
        return emailVerificationRepository;
    }

    /**
     * Validates the change of password.
     * This method always returns {@code false} as it is not used for email verification.
     * Subclasses do not need to override this method unless they intend to use it for password change validation.
     *
     * @return {@code false} indicating that the password change is not valid
     */
    @Override
    protected boolean validatePasswordChange(User user, String oldPassword, String newPassword, String confirmPassword) {
        return false;
    }

    /**
     * Logs out the user from the current session.
     * This method is not used for email verification, so it remains empty.
     * Subclasses do not need to override this method unless they intend to use it for user logout.
     *
     */
    @Override
    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // This method is not used for email verification, so it remains empty
    }

// This method is not used rn
//        public void createTokenForUser(User user, String token) {
//        EmailVerificationToken emailVerificationToken = new EmailVerificationToken(token, user);
//        emailVerificationRepository.save(emailVerificationToken);
//        log.info("Token generated for the user - in EmailVerificationServiceImpl");
//    }



    /**
     * Checks the validity of the registration token and performs user registration if the token is valid.
     *
     * @param token The registration token to be validated.
     * @return A string indicating the validation result:
     *          - "valid" if the token is valid and registration is successful,
     *          - "invalid" if the token is invalid, or
 *              - "expired" if the token has expired.
     * @throws RuntimeException If an error occurs while sending the email verification notification.
     */
    @Override
    public String checkAndValidateRegistration(String token) {
        // Find the email verification token in the repository
        Optional<EmailVerificationToken> optionalEmailVerificationToken = emailVerificationRepository.findByToken(token);

        EmailVerificationToken emailVerificationToken = null;
        if (optionalEmailVerificationToken.isPresent()){
            emailVerificationToken = optionalEmailVerificationToken.get();
        }
        // Check if the token is invalid
        if(emailVerificationToken == null || !emailVerificationToken.isValid()){
            log.info("EmailVerificationServiceImpl::checkAndValidateRegistration - INVALID TOKEN: {}", token);
            return "invalid";
        }

        // Retrieve the user associated with the token
        User user = emailVerificationToken.getUser();

        // Check if the token has expired
        LocalDateTime emailVerificationTokenExpirationTime = emailVerificationToken.getRequestTime();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm:ss");
        log.info("EmailVerificationServiceImpl::checkAndValidateRegistration - ExpTime: {}", emailVerificationTokenExpirationTime.format(formatter));
        log.info("EmailVerificationServiceImpl::checkAndValidateRegistration - Time now: {}", now.format(formatter));

        if (now.isAfter(emailVerificationTokenExpirationTime)){
            log.info("EmailVerificationServiceImpl::checkAndValidateRegistration - EXPIRED TOKEN: {}", token);
            return "expired";
        }
        // Enable the user and save the changes
        user.setEnabled(true);
        userRepository.save(user);

        // Mark the token as invalid and save the changes
        emailVerificationToken.setValid(false);
        emailVerificationRepository.save(emailVerificationToken);

        // Send the email notification for successful email verification
        try {
            emailSender.sendEmailVerificationWasSuccessful(user);
            log.info("EmailVerificationServiceImpl::checkAndValidateRegistration - VALID, SENT email to user: {}", user.getEmail());
            return "valid";
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("EmailVerificationServiceImpl::checkAndValidateRegistration - something went wrong while sending EmailVerificationWasSuccessful: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds an email verification token by its token string.
     *
     * @param token the token string to search for
     * @return an {@link Optional} containing the email verification token
     */
    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return emailVerificationRepository.findByToken(token);
    }


}