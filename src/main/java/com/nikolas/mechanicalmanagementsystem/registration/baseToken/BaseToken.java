package com.nikolas.mechanicalmanagementsystem.registration.baseToken;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public class BaseToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @ManyToOne
    private User user;
    private LocalDateTime requestTime;
    private boolean isValid = true;

    /**
     * Constructs a new instance of BaseToken with the current timestamp as the request time and the token initially set as valid.
     */
    public BaseToken() {
        this.requestTime = LocalDateTime.now();
        this.isValid = true;
    }

    /**
     * Constructs a new instance of BaseToken with the specified token, user, and request time.
     *
     * @param token         the token string representing the token
     * @param user          the user associated with the token
     * @param requestTime   the timestamp indicating when the token was requested
     */
    public BaseToken(String token, User user, LocalDateTime requestTime) {
        this.token = token;
        this.user = user;
        this.requestTime = requestTime;
    }
}
