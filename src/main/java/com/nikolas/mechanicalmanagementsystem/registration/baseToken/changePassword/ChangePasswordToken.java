package com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword;

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
public class ChangePasswordToken extends BaseToken {

    /**
     * Constructs a new instance of ChangePasswordToken with the specified token, user, and expiration time.
     *
     * @param token         the token string representing the password change token
     * @param user          the user associated with the token
     */
    public ChangePasswordToken(String token, User user) {
//        Change PW Token expiration time = 3 minutes
        super(token, user, LocalDateTime.now().plusMinutes(3));
    }
}
