package com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseToken;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ForgotPasswordToken extends BaseToken {

    /**
     * Constructs a new instance of ForgotPasswordToken with the specified token, user, and expiration time.
     *
     * @param token         the token string representing the forgot password token
     * @param user          the user associated with the token
     */
    public ForgotPasswordToken(String token, User user) {
        // Forgot PW Token expiration time = 3 minutes
        super(token, user, LocalDateTime.now().plusMinutes(3));
    }
}
