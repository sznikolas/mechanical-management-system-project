package com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenServiceImpl;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A service class responsible for handling forgot password operations.
 * Extends {@link BaseTokenServiceImpl} to utilize basic token-based functionality.
 * Implements {@link ForgotPasswordService} to provide forgot password methods.
 */
@Service
public class ForgotPasswordServiceImpl extends BaseTokenServiceImpl<ForgotPasswordToken, ForgotPasswordRepository> implements ForgotPasswordService{
    private final ForgotPasswordRepository forgotPasswordRepository;

    /**
     * Constructs a new ForgotPasswordServiceImpl instance.
     *
     * @param forgotPasswordRepository the repository for handling forgot password related data access
     */
    public ForgotPasswordServiceImpl(ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder,
                                     UserRepository userRepository, EmailSender eventListener) {
        super(forgotPasswordRepository, passwordEncoder, userRepository, eventListener);
        this.forgotPasswordRepository = forgotPasswordRepository;
    }

    /**
     * Creates a new instance of the token for forgot password.
     *
     * @param user  the user associated with the token
     * @param token the token string
     * @return a new instance of {@link ForgotPasswordToken}
     */
    @Override
    protected ForgotPasswordToken createTokenInstance(User user, String token) {
        return new ForgotPasswordToken(token, user);
    }

    /**
     * Retrieves the token repository for forgot password tokens.
     *
     * @return the {@link ForgotPasswordRepository} instance
     */
    @Override
    protected JpaRepository<ForgotPasswordToken, Long> getTokenRepository() {
        return forgotPasswordRepository;
    }

    /**
     * Validates a password change request by checking if the new password matches the confirmed new password.
     * This method does not validate the old password as it is not required for forgot password operations.
     *
     * @param user           the user whose password is being changed
     * @param oldPassword    the old password (not used in forgot password operations, can be {@code null})
     * @param newPassword    the new password
     * @param confirmPassword the confirmed new password
     * @return {@code true} if the password change request is valid, {@code false} otherwise
     */
    @Override
    protected boolean validatePasswordChange(User user, String oldPassword, String newPassword, String confirmPassword) {
        return newPassword.equals(confirmPassword);
    }

    /**
     * Logs out the user after a password change request.
     *
     * @param request  the {@link HttpServletRequest} object
     * @param response the {@link HttpServletResponse} object
     */
    @Override
    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // No logout action needed for forgot password operation
    }

}
