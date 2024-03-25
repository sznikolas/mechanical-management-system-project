package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.UnauthorizedException;
import com.nikolas.mechanicalmanagementsystem.registration.RegistrationRequest;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword.ChangePasswordService;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword.ForgotPasswordService;
import com.nikolas.mechanicalmanagementsystem.registration.emailVerification.EmailVerificationService;
import com.nikolas.mechanicalmanagementsystem.repository.JobApplicationRepository;
import com.nikolas.mechanicalmanagementsystem.repository.RoleRepository;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import com.nikolas.mechanicalmanagementsystem.security.CustomUserDetails;
import com.nikolas.mechanicalmanagementsystem.utility.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JobApplicationRepository jobApplicationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailSender emailSender;
    private final RoleService roleService;
    private final ChangePasswordService changePasswordService;
    private final ForgotPasswordService forgotPasswordService;



    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Registers a new user in the system based on the provided registration request.
     *
     * @param registration The registration request containing the user's details.
     * @return The newly registered user.
     */
    @Override
    public User registerUser(RegistrationRequest registration) {
        // Check that the role exists in the database
        Role userRole = roleRepository.findByRoleName(UserRoleEnum.USER.name()).orElse(null);

        // If the role does not exist, create one
        if (userRole == null) {
            userRole = new Role(UserRoleEnum.USER.name());
            roleRepository.save(userRole);
        }
        User user = User.builder()
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .password(passwordEncoder.encode(registration.getPassword()))
                .isAccountNonLocked(true)
                .roles(Set.of(userRole))
                .build();

        log.info("UserServiceImpl::registerUser - {} has successfully registered in the system!", user.getLastName() + ' ' + user.getFirstName());
        userRepository.save(user);
        return user;

    }

    /**
     * Handles the registration request submitted by the user.
     * <p>
     * This method processes the registration request, including validation, checking for existing users,
     * registering the new user, generating and sending an email verification token, and sending a verification email.
     *
     * @param registration The registration request submitted by the user.
     * @param result       The binding result for validation errors.
     * @param request      The HTTP servlet request.
     * @return A string indicating the result of the registration process:
     * - "invalidEmailFormat" if the email format is invalid.
     * - "exist" if a user with the provided email already exists.
     * - "success" if the registration process is successful.
     * - "emailSendingError" if an error occurs while sending the verification email.
     */
    @Override
    @Transactional
    public String handleRegistrationRequest(RegistrationRequest registration, BindingResult result, HttpServletRequest request) {
        // Construct the URL for email verification
        String url = UrlUtil.getApplicationUrl(request);
        String token = UUID.randomUUID().toString();
        url = url + "/registration/verifyEmail?token=" + token;

        // Validate the email format
        if (result.hasErrors()) {
            log.warn("UserServiceImpl::handleRegistrationRequest - Invalid email format: {}", registration.getEmail());
            return "invalidEmailFormat";
        }
        // Check if the user already exists
        if (findByEmail(registration.getEmail()).isPresent()) {
            log.warn("UserServiceImpl::handleRegistrationRequest - User with this email already exist! - {} ", registration.getEmail());
            return "exist";
        }

        log.info("UserServiceImpl::handleRegistrationRequest - User registration request accepted!");
        // Register the user
        User user = registerUser(registration);
        // Create and send email verification token
        emailVerificationService.createTokenForUser(user, token);
        try {
            emailSender.sendVerificationEmail(user, url);
            log.info("UserServiceImpl::handleRegistrationRequest - Email sent to user: {}", user.getEmail());
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("UserServiceImpl::handleRegistrationRequest - An error occurred during registration!");
            return "emailSendingError";
        }
        return "success";
    }


    /**
     * Checks if a user with the specified email address already exists in the database.
     *
     * @param email The email address to check for existence.
     * @return {@code true} if a user with the specified email address exists, {@code false} otherwise.
     */


    //    Optional find by email
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // save the user to DB
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsersExceptCurrentUser(User currentUser) {
        List<User> allUsers = userRepository.findAll();
        log.info("UserServiceImpl::getAllUsersExceptCurrentUser - All users: {}", allUsers);
        log.info("UserServiceImpl::getAllUsersExceptCurrentUser - Current user: {}", currentUser);
        // Remove my user from the list
        allUsers.removeIf(user -> user.getId().equals(currentUser.getId()));
        return allUsers;
    }


    /**
     * Retrieves the currently logged-in user from the security context.
     *
     * @return The User object representing the currently logged-in user.
     * @throws UnauthorizedException if the user is not authenticated or user details are not available.
     * @throws NotFoundException     if the user cannot be found with the retrieved email address.
     */
    @Override
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If authentication is null or not authenticated, throw an UnauthorizedException
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("UserServiceImpl::getLoggedInUser - User is not authenticated!");
            throw new UnauthorizedException("User is not authenticated");
        }
        // If the principal is not an instance of CustomUserDetails, throw an UnauthorizedException
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            log.warn("UserServiceImpl::getLoggedInUser - principal is not an instance of CustomUserDetails!");
            throw new UnauthorizedException("User details are not available");
        }

        String email = userDetails.getEmail();
        // Retrieve the user by email address from the repository, or throw a NotFoundException
        return findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Toggles the locked status of a user account identified by the provided userId.
     * If the account is currently locked, it will be unlocked, and vice versa.
     *
     * @param userId The ID of the user account to toggle.
     * @return The updated User object with the toggled locked status.
     * @throws NotFoundException if no user is found with the given userId.
     */
    @Override
    public User toggleIsAccountNonLocked(Long userId) {
        return findById(userId).map(user -> {
            // Toggle the account locked status
            user.setAccountNonLocked(!user.isAccountNonLocked());
            log.info("UserServiceImpl::toggleIsAccountNonLocked - The active status of the user {} has changed to {}",
                    user.getLastName() + " " + user.getFirstName(), user.isAccountNonLocked() ? "unlocked" : "locked");
            return userRepository.save(user);
        }).orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    /**
     * Deletes a user identified by the provided userId from the system.
     * This operation removes the user's roles, removes the user from any associated companies,
     * deletes the user's job applications, and deletes any associated email verification, change password, and forgot password tokens.
     * Finally, it deletes the user from the database.
     *
     * @param userId The ID of the user to be deleted.
     * @throws NotFoundException if no user is found with the given userId.
     */
    @Transactional
    @Override
    public void deleteUserById(Long userId) {
        // Find the user by ID or throw NotFoundException if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Remove user from associated companies
        for (Company company : user.getWorkplaces()) {
            company.getEmployees().remove(user);
        }

        // Delete user's job applications
        Set<JobApplication> jobApplications = user.getJobApplications();
        jobApplicationRepository.deleteAll(jobApplications);

        // Delete user's tokens (email verification, change PW, forgot PW)
        emailVerificationService.deleteTokensByUserId(userId);
        changePasswordService.deleteTokensByUserId(userId);
        forgotPasswordService.deleteTokensByUserId(userId);

        // Delete the user from the database
        userRepository.delete(user);
        log.info("UserServiceImpl::deleteUserById - The {} user has been deleted from the system!", (user.getLastName() + " " + user.getFirstName()));
    }

    /**
     * Updates the details of the current user in the system.
     *
     * @param currentUser The currently logged-in user whose details are being updated.
     * @param userDetails The updated details of the user.
     * @throws NotFoundException     If the user with the provided email is not found in the database.
     * @throws AccessDeniedException If access to update the user details is denied.
     */
    @Transactional
    @Override
    public void updateUserDetails(User currentUser, User userDetails) {
        // Retrieve the user object from the database based on the email of the currently logged-in user
        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + currentUser.getEmail()));
        // Check if the current user ID matches the ID of the user whose details are being updated
        if (!currentUser.getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("Access is denied");
        }

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        // Additional data updates can be performed here...

        // Save the updated user details to the database
        userRepository.save(user);
        log.info("UserServiceImpl::updateUserDetails - The user {} changed his profile details (only name)!", user.getLastName() + ' ' + user.getFirstName());

    }

    /**
     * Updates the roles of a user.
     *
     * @param userId        The ID of the user whose roles are being updated.
     * @param selectedRoles The list of role names to be assigned to the user.
     * @throws NotFoundException If the user with the given ID is not found.
     */
    @Override
    public void updateUserRoles(Long userId, List<String> selectedRoles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        // Remove roles that are not selected
        user.getRoles().removeIf(role -> !selectedRoles.contains(role.getRoleName()));

        // Add selected roles that are not already assigned to the user
        for (String roleName : selectedRoles) {
            Role roleToAdd = roleService.getRoleByName(roleName);
            if (roleToAdd != null && !user.getRoles().contains(roleToAdd)) {
                user.getRoles().add(roleToAdd);
            }
        }
        // Save the user with updated roles
        saveUser(user);
        // Log the update
        log.info("UserServiceImpl::updateUserRoles - The roles of the user {} have been updated!", user.getLastName() + " " + user.getFirstName());
    }


    /**
     * Removes the specified roles from all users who have them.
     *
     * @param rolesToRemove The list of roles to be removed from users.
     */
    @Transactional
    @Override
    public void removeRolesFromUsers(List<Role> rolesToRemove) {
        // Iterate through each role to remove
        for (Role role : rolesToRemove) {
            // Find all users who have the specified role
            List<User> usersWithRole  = userRepository.findByRolesContains(role);
            // Iterate through each user with the role
            for (User user : usersWithRole ) {
                // Remove the role from the user's roles
                user.getRoles().remove(role);
                // Save the user with updated roles
                saveUser(user);
            }
        }
        log.info("UserServiceImpl::removeRolesFromUsers - The roles {} have been removed from all users.", rolesToRemove.stream().map(Role::getRoleName).collect(Collectors.toList()));
    }

    public List<Company> getWorkplacesForLoggedInUser(String userEmail) {
        return userRepository.findWorkplacesByEmail(userEmail);
    }

}