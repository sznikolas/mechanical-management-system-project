package com.nikolas.mechanicalmanagementsystem.registration.emailVerification;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseToken;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class EmailVerificationToken extends BaseToken {
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private boolean isValid;


    /**
     * Constructs an email verification token with the specified token string and user.
     * Sets the expiration time to x minutes from the current time.
     *
     * @param token the token string for email verification
     * @param user  the user associated with the token
     */
    public EmailVerificationToken(String token, User user) {
        // Set the token, user, and expiration time
        super(token, user, LocalDateTime.now().plusMinutes(2));
        this.user = user;
        this.isValid = true;
    }
}
