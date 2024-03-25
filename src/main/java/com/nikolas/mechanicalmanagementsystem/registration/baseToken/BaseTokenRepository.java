package com.nikolas.mechanicalmanagementsystem.registration.baseToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseTokenRepository<T extends BaseToken> extends JpaRepository<T, Long> {
    Optional<T> findByToken(String token);

    List<T> findByUserId(Long userId);
    void deleteTokensByUserId(Long userId);
}
