package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.registration.RegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    List<User> findAllUsers();
    User registerUser(RegistrationRequest registrationRequest);
    String handleRegistrationRequest(RegistrationRequest registration, BindingResult result, HttpServletRequest request);
    Optional<User> findByEmail(String email);
    void saveUser(User user);
    List<User> getAllUsersExceptCurrentUser(User currentUser);
    Optional<User> findById(Long id);
    User getLoggedInUser();
    User toggleIsAccountNonLocked(Long userId);
    void deleteUserById(Long userId);
    void updateUserDetails(User userDetails, User updatedUser);
    void updateUserRoles(Long userId, List<String> selectedRoles);
    void removeRolesFromUsers(List<Role> rolesToRemove);
    List<Company> getWorkplacesForLoggedInUser(String userEmail);

}
