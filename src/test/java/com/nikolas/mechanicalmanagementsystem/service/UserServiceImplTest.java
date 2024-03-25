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
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userServiceImpl;
    @Mock
    private CustomUserDetails customUserDetails;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    //DONT delete it
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private EmailSender emailSender;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private ChangePasswordService changePasswordService;
    @Mock
    private ForgotPasswordService forgotPasswordService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RoleService roleService;

    private static User user1;
    private static User user2;

    private static final RegistrationRequest registration = new RegistrationRequest();


    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("ROLE_ADMIN"));
        roles.add(new Role("ROLE_USER"));
        roles.add(new Role("ROLE_EXAMPLE"));

        List<Company> workplaces = new ArrayList<>();
        Company testCompany = new Company();
        testCompany.setCompanyName("Test Company");

        Set<User> employees = new HashSet<>();


        testCompany.setEmployees(employees);
        workplaces.add(testCompany);

        Set<JobApplication> jobApplications = new HashSet<>();
        JobApplication testJobApplication = new JobApplication();
        jobApplications.add(testJobApplication);

        user1 = User.builder()
                .id(1L)
                .firstName("ADMIN")
                .lastName("ADMIN")
                .email("admin@gmail.com")
                .password("admin")
                .isEnabled(true)
                .isAccountNonLocked(true)
                .roles(roles)
                .registrationDate(LocalDateTime.now())
                .jobApplications(jobApplications)
                .ownedCompanies(new HashSet<>())
                .workplaces(workplaces)
                .build();

        user2 = User.builder()
                .id(2L)
                .firstName("USER")
                .lastName("USER")
                .email("user@gmail.com")
                .password("user")
                .isEnabled(true)
                .isAccountNonLocked(true)
                .roles(Set.of(new Role(UserRoleEnum.USER.name())))
                .registrationDate(LocalDateTime.now())
                .jobApplications(jobApplications)
                .ownedCompanies(new HashSet<>())
                .workplaces(workplaces)
                .build();

        employees.add(user1);
        employees.add(user2);
    }

    @Test
    void testFindAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        // Mock behavior
        when(userServiceImpl.findAllUsers()).thenReturn(users);
        users.forEach(user -> System.out.println(user.getFirstName() + " " + user.getLastName()));
        List<User> foundUsers = userServiceImpl.findAllUsers();
        assertEquals(users.size(), foundUsers.size());
    }

    @Test
    void testRegisterUser() {
        // Arrange
        registration.setFirstName("John");
        registration.setLastName("Doe");
        registration.setEmail("john@example.com");
        registration.setPassword("password");

        // Act
        User registeredUser = userServiceImpl.registerUser(registration);
        System.out.println("registeredUser roles: " + registeredUser.getRoles().stream().map(Role::getRoleName).toList());

        // Assert
        assertNotNull(registeredUser, "Registered user should not be null");
        assertEquals(registeredUser.getFirstName(), registration.getFirstName(), "First name should match");
        assertEquals(registeredUser.getLastName(), registration.getLastName(), "Last name should match");
        assertEquals(registeredUser.getEmail(), registration.getEmail(), "Email should match");
        assertEquals(registeredUser.getPassword(), passwordEncoder.encode(registration.getPassword()), "Password should match");
        assertFalse(registeredUser.isEnabled(), "User should be disabled");
        assertTrue(registeredUser.isAccountNonLocked(), "User account should not be locked");

        // Check roles
        Set<Role> roles = registeredUser.getRoles();
        assertNotNull(roles, "User roles should not be null");
        assertEquals(1, roles.size(), "User should have only one role");
        Role userRole = roles.stream().findFirst().orElse(null);
        assertNotNull(userRole, "User role should not be null");
        assertEquals(UserRoleEnum.USER.name(), userRole.getRoleName(), "User should have the USER role");
    }

    @Test
    void testHandleRegistrationRequest_SuccessfulRegistration(){
        // Arrange
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setFirstName("Registration");
        registrationRequest.setLastName("Test");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());

        // Create a mock HttpServletRequest object with a dummy URL
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(httpServletRequest.getServletPath()).thenReturn("/system");

        // Act
        String result = userServiceImpl.handleRegistrationRequest(registrationRequest, bindingResult, httpServletRequest);

        // Assert
        assertEquals("success", result);
        verify(bindingResult, times(1)).hasErrors();
        verify(userRepository, times(1)).findByEmail(registrationRequest.getEmail());
    }

    @Test
    void testHandleRegistrationRequest_InvalidEmailFormat() {
        // Arrange
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("invalid-email@test.c");

        when(bindingResult.hasErrors()).thenReturn(true);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(httpServletRequest.getServletPath()).thenReturn("/system");

        // Act
        String result = userServiceImpl.handleRegistrationRequest(registrationRequest, bindingResult, httpServletRequest);

        // Assert
        assertEquals("invalidEmailFormat", result);
        verify(bindingResult, times(1)).hasErrors();
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testHandleRegistrationRequest_UserAlreadyExists(){
        // Arrange
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("existing-email@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);

        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(new User()));

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(httpServletRequest.getServletPath()).thenReturn("/system");

        // Act
        String result = userServiceImpl.handleRegistrationRequest(registrationRequest, bindingResult, httpServletRequest);

        // Assert
        assertEquals("exist", result);
        verify(userRepository, times(1)).findByEmail(registrationRequest.getEmail());
    }

    @Test
    void testHandleRegistrationRequest_EmailSendingError() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("valid-email@example.com");
        registrationRequest.setLastName("Error");
        registrationRequest.setFirstName("Occurred");

        when(bindingResult.hasErrors()).thenReturn(false);

        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(httpServletRequest.getServletPath()).thenReturn("/system");

        doThrow(new MessagingException()).when(emailSender).sendVerificationEmail(any(User.class), anyString());

        // Act
        String result = userServiceImpl.handleRegistrationRequest(registrationRequest, bindingResult, httpServletRequest);

        // Assert
        assertEquals("emailSendingError", result);
        verify(emailSender, times(1)).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void testFindByEmail_UserExist() {
        // Arrange
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
        // Act
        Optional<User> user = userServiceImpl.findByEmail(user1.getEmail());
        // Assert
        assertNotNull(user);
        assertTrue(user.isPresent());
        assertEquals(user1, user.get());
    }

    @Test
    void testFindByEmail_UserDoesNotExist() {
        String nonExistingEmail = "nonexisting@example.com";

        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());
        Optional<User> user = userServiceImpl.findByEmail(nonExistingEmail);
        assertTrue(user.isEmpty());
    }

    @Test
    public void testSaveUser() {
        userServiceImpl.saveUser(user1);
        // Assert
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testGetAllUsersExceptCurrentUser() {
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        when(userRepository.findAll()).thenReturn(userList);
        List<User> result = userServiceImpl.getAllUsersExceptCurrentUser(user2);

        // Assert that the result does not contain the current user
        assertFalse(result.stream().anyMatch(user -> user.getId().equals(user2.getId())));
    }

    @Test
    void testGetLoggedInUser() {
        // Set up the email attribute
        when(customUserDetails.getEmail()).thenReturn(user1.getEmail());
        // Simulate authentication being successful
        when(authentication.isAuthenticated()).thenReturn(true);
        // Set up the authentication object to return the custom user details
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        // Set up the security context to return the authentication object
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Set the security context in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
        // Mock the UserRepository to return the user object when findByEmail is called
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));

        // Call the getLoggedInUser method
        User loggedInUser = userServiceImpl.getLoggedInUser();

        // Assert that the returned user is not null and matches the expected user
        assertNotNull(loggedInUser);
        assertEquals(user1, loggedInUser);
    }

    @Test
    void testGetLoggedInUser_UnauthenticatedUser() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> userServiceImpl.getLoggedInUser());

        // Verify
        verify(authentication, times(1)).isAuthenticated();
        verifyNoInteractions(authenticationManager); // AuthenticationManager should not be called
    }

    @Test
    void testGetLoggedInUser_AuthenticatedUser_NotCustomUserDetailsInstance() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        Object principal = mock(UserDetails.class); // we use UserDetails instead of CustomUserDetails
        when(authentication.getPrincipal()).thenReturn(principal);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> userServiceImpl.getLoggedInUser());

        // Verify
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
        verifyNoInteractions(authenticationManager); // AuthenticationManager should not be called
    }


    @Test
    void testFindUserById() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        Optional<User> result = userServiceImpl.findById(user1.getId());

        assertEquals(Optional.of(user1), result);
        assertEquals(user1.getId(), result.get().getId());
    }

    @Test
    void testToggleIsAccountNonLocked() {
        // Arrange
        user1.setAccountNonLocked(true); // Initially set to true for testing

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved user

        // Act
        User toggledUser = userServiceImpl.toggleIsAccountNonLocked(user1.getId());

        // Assert
        assertNotNull(toggledUser);
        assertFalse(toggledUser.isAccountNonLocked()); // Expecting it to be toggled to false

        // Verify that findById and userRepository.save methods were called once
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testDeleteUserById() {
        Company company = user1.getWorkplaces().get(0);
        Set<User> employees = company.getEmployees();

        // Mock repository methods
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        doNothing().when(jobApplicationRepository).deleteAll(user1.getJobApplications());
        doNothing().when(emailVerificationService).deleteTokensByUserId(user1.getId());
        doNothing().when(changePasswordService).deleteTokensByUserId(user1.getId());
        doNothing().when(forgotPasswordService).deleteTokensByUserId(user1.getId());
        doNothing().when(userRepository).delete(user1);

        // Act
        System.out.println("User company: " + user1.getWorkplaces().stream().map(Company::getCompanyName).toList());
        System.out.println(user1.getWorkplaces().stream().map(Company::getCompanyName).toList() + " employees before delete user from system: " + employees.stream().map(user -> user.getLastName() + " " + user.getFirstName()).toList());
        userServiceImpl.deleteUserById(user1.getId());
        System.out.println(user1.getWorkplaces().stream().map(Company::getCompanyName).toList() + " employees after delete user from system: "  + company.getEmployees().stream().map(user -> user.getLastName() + " " + user.getFirstName()).toList());

        // Verify that the remove method is called for each company's employees
        verify(userRepository, times(1)).findById(user1.getId());
        verify(jobApplicationRepository, times(1)).deleteAll(any());
        verify(emailVerificationService, times(1)).deleteTokensByUserId(user1.getId());
        verify(changePasswordService, times(1)).deleteTokensByUserId(user1.getId());
        verify(forgotPasswordService, times(1)).deleteTokensByUserId(user1.getId());
        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void testUpdateUserDetails() {
        // Arrange
        User userDetails = new User();
        userDetails.setId(1L);
        userDetails.setFirstName("User");
        userDetails.setLastName("Test");

        // Mock userRepository
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));

        // Act
        System.out.println("User name before modify name: " + user1.getLastName() + " " + user1.getFirstName());
        userServiceImpl.updateUserDetails(user1, userDetails);
        System.out.println("User name after modify name: " + user1.getLastName() + " " + user1.getFirstName());

        // Assert
        verify(userRepository, times(1)).findByEmail(user1.getEmail());
        verify(userRepository, times(1)).save(user1);
        assertEquals(userDetails.getFirstName(), user1.getFirstName());
        assertEquals(userDetails.getLastName(), user1.getLastName());
    }

    @Test
    void testUpdateUserDetails_AccessDenied() {
        // Arrange
        User userDetails = new User();
        userDetails.setId(2L); // Set different user's ID

        // Mock repository to return a user with the same email as the current user
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> userServiceImpl.updateUserDetails(user1, userDetails));

        // Verify that the userRepository.findByEmail() method was called once
        verify(userRepository, times(1)).findByEmail(user1.getEmail());
    }

    @Test
    void testUpdateUserDetails_UserNotFound() {
        // Arrange
        User userDetails = new User();
        userDetails.setId(1L);

        // Mock repository to return an empty Optional, indicating user not found
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userServiceImpl.updateUserDetails(user1, userDetails));

        // Verify that the userRepository.findByEmail() method was called once
        verify(userRepository, times(1)).findByEmail(user1.getEmail());
    }


    @Test
    void testUpdateUserRoles() {
        // Arrange
        List<String> selectedRoles = List.of("ROLE_ADMIN", "ROLE_USER");

        Role adminRole = new Role("ROLE_ADMIN");
        Role userRole = new Role("ROLE_USER");
        Role roleNotSelected = new Role("ROLE_NOT_SELECTED");

        // Mock userRepository findById method
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        // Mock roleService getRoleByName method
        when(roleService.getRoleByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(roleService.getRoleByName("ROLE_USER")).thenReturn(userRole);

        // Act
        System.out.println("User roles before update: " + user1.getRoles().stream().map(Role::getRoleName).toList());
        userServiceImpl.updateUserRoles(user1.getId(), selectedRoles);
        System.out.println("User roles after update: " + user1.getRoles().stream().map(Role::getRoleName).toList());

        // Assertions
        assertEquals(2, user1.getRoles().size());
        assertTrue(user1.getRoles().contains(adminRole));
        assertTrue(user1.getRoles().contains(userRole));
        assertFalse(user1.getRoles().contains(roleNotSelected));
        verify(userRepository, times(1)).findById(user1.getId());
        verify(roleService, times(2)).getRoleByName(anyString());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testRemoveRolesFromUsers_WhenDeleteACompany() {
        // Arrange
        Role roleToRemove = new Role("ROLE_ADMIN");

        // Mock repository methods
        when(userRepository.findByRolesContains(roleToRemove)).thenReturn(List.of(user1));

        // Act
        System.out.println("User roles before delete a role: " + user1.getRoles().stream().map(Role::getRoleName).toList());
        userServiceImpl.removeRolesFromUsers(List.of(roleToRemove));
        System.out.println("User roles after delete a role: " + user1.getRoles().stream().map(Role::getRoleName).toList());

        // Assert
        assertFalse(user1.getRoles().contains(roleToRemove));
        verify(userRepository, times(1)).findByRolesContains(roleToRemove);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testGetWorkplacesForLoggedInUser() {
        // Mock repository method
        when(userRepository.findWorkplacesByEmail(user1.getEmail())).thenReturn(user1.getWorkplaces());

        // Act
        List<Company> actualWorkplaces = userServiceImpl.getWorkplacesForLoggedInUser(user1.getEmail());
        System.out.println("User workplaces: " + user1.getWorkplaces().stream().map(Company::getCompanyName).toList());
        System.out.println("Actual workplaces: " + actualWorkplaces.stream().map(Company::getCompanyName).toList());

        // Assert
        assertEquals(1, actualWorkplaces.size());
        assertTrue(actualWorkplaces.containsAll(user1.getWorkplaces()));
        verify(userRepository, times(1)).findWorkplacesByEmail(user1.getEmail());
    }


}
