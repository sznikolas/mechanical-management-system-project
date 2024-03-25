package com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword;

import com.nikolas.mechanicalmanagementsystem.registration.baseToken.BaseTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangePasswordRepository extends BaseTokenRepository<ChangePasswordToken> {
    List<ChangePasswordToken> findByUserId(Long userId);
}
