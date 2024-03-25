package com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword;

import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForgotPasswordRepository extends BaseTokenRepository<ForgotPasswordToken> {
    List<ForgotPasswordToken> findByUserId(Long userId);
}
