package com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenServiceImpl;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;


/**
 * A service class responsible for handling password change operations.
 * Extends {@link BaseTokenServiceImpl} to utilize basic token-based functionality.
 * Implements {@link ChangePasswordService} to provide password change methods.
 */
@Service
public class ChangePasswordServiceImpl extends BaseTokenServiceImpl<ChangePasswordToken, ChangePasswordRepository> implements ChangePasswordService {
    private final ChangePasswordRepository changePasswordRepository;
    private final SecurityContextLogoutHandler logoutHandler;


    /**
     * Constructs a new ChangePasswordServiceImpl instance.
     *
     * @param changePasswordRepository the repository for handling password change related data access
     */
    public ChangePasswordServiceImpl(ChangePasswordRepository changePasswordRepository, PasswordEncoder passwordEncoder,
                                     UserRepository userRepository, EmailSender eventListener,
                                     SecurityContextLogoutHandler logoutHandler) {
        super(changePasswordRepository, passwordEncoder, userRepository, eventListener);
        this.changePasswordRepository = changePasswordRepository;
        this.logoutHandler = logoutHandler;
    }

    /**
     * Constructs a new instance of the token for password change.
     *
     * @param user  the user associated with the token
     * @param token the token string
     * @return a new instance of {@link ChangePasswordToken}
     */
    @Override
    protected ChangePasswordToken createTokenInstance(User user, String token) {
        return new ChangePasswordToken(token, user);
    }

    /**
     * Retrieves the token repository for password change tokens.
     *
     * @return the {@link ChangePasswordRepository} instance
     */
    @Override
    protected JpaRepository<ChangePasswordToken, Long> getTokenRepository() {
        return changePasswordRepository;
    }

    /**
     * Validates the password change request.
     *
     * @param user           the user whose password is being changed
     * @param oldPassword    the old password
     * @param newPassword    the new password
     * @param confirmPassword the confirmed new password
     * @return {@code true} if the password change request is valid, {@code false} otherwise
     */
    @Override
    protected boolean validatePasswordChange(User user, String oldPassword, String newPassword, String confirmPassword) {
        return newPassword.equals(confirmPassword) && passwordEncoder.matches(oldPassword, user.getPassword());
    }

    /**
     * Logs out the user after a password change request.
     *
     * @param request  the {@link HttpServletRequest} object
     * @param response the {@link HttpServletResponse} object
     */
//    @Override
//    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
//        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
//    }

    @Override
    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }


}
