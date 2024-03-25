package com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceImplTest {

    @InjectMocks
    private ForgotPasswordServiceImpl forgotPasswordServiceImpl;
    @Mock
    private ForgotPasswordRepository forgotPasswordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailSender eventListener;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @Test
    void testCheckTokenValidity_ExpiredToken() {
        // Arrange
        String expiredToken = "expired_token";
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(1);
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setRequestTime(expirationTime);
        when(forgotPasswordRepository.findByToken(expiredToken)).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        String result = forgotPasswordServiceImpl.checkTokenValidity(expiredToken);
        // Assert
        assertEquals("EXPIRED", result);
        assertTrue(forgotPasswordToken.isValid());
    }

    @Test
    void testCheckTokenValidity_InvalidToken() {
        // Arrange
        String invalidToken = "invalid_token";
        when(forgotPasswordRepository.findByToken(invalidToken)).thenReturn(Optional.empty());
        // Act
        String result = forgotPasswordServiceImpl.checkTokenValidity(invalidToken);
        // Assert
        assertEquals("INVALID", result);
    }

    @Test
    void testCheckTokenValidity_ValidToken() {
        // Arrange
        String validToken = "valid_token";
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setRequestTime(expirationTime);
        when(forgotPasswordRepository.findByToken(validToken)).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        String result = forgotPasswordServiceImpl.checkTokenValidity(validToken);
        // Assert
        assertEquals("VALID", result);
        assertFalse(forgotPasswordToken.isValid());
    }

    @Test
    void testFindUserByToken_ExistingToken() {
        // Arrange
        String existingToken = "existing_token";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        when(forgotPasswordRepository.findByToken(existingToken)).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        Optional<User> result = forgotPasswordServiceImpl.findUserByToken(existingToken, forgotPasswordRepository);
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testFindUserByToken_NonExistingToken() {
        // Arrange
        String nonExistingToken = "non_existing_token";
        when(forgotPasswordRepository.findByToken(nonExistingToken)).thenReturn(Optional.empty());
        // Act
        Optional<User> result = forgotPasswordServiceImpl.findUserByToken(nonExistingToken, forgotPasswordRepository);
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testChangePassword_Successful() {
        // Arrange
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("old_pw");
        String newPassword = "new_password";

        // Mocking the behavior of passwordEncoder
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded_new_password");

        // Act
        System.out.println("pw before change: " + user.getPassword());
        forgotPasswordServiceImpl.changePassword(user, newPassword);
        System.out.println("pw after change: " + user.getPassword());

        // Assert
        assertEquals("encoded_new_password", user.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user); //

    }

    @Test
    public void testCreateTokenForUser() {
        // Arrange
        User user = new User();
        String token = "testToken";
        // Mock
        when(forgotPasswordRepository.save(any())).thenReturn(null);
        // Act
        forgotPasswordServiceImpl.createTokenForUser(user, token);
        // Assert
        verify(forgotPasswordRepository, times(1)).save(any());
    }

    @Test
    public void testDeleteTokensByUserId() {
        // Arrange
        Long userId = 123L;
        // Mock
        List<ForgotPasswordToken> tokens = new ArrayList<>();
        when(forgotPasswordRepository.findByUserId(userId)).thenReturn(tokens);
        // Act
        forgotPasswordServiceImpl.deleteTokensByUserId(userId);
        // Assert
        verify(forgotPasswordRepository, times(1)).findByUserId(userId);
        verify(forgotPasswordRepository, times(1)).deleteAll(tokens);
    }


    @Test
    public void testProcessRequestAndSendEmail_EmailNotFound() {
        // Arrange
        String email = "test@example.com";
        // Set up the security context to return the authentication object
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Set the security context in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/registration/sendVerificationToken");
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("email_not_found", result);
    }

    @Test
    public void testProcessRequestAndSendEmail_ResendVerificationEmail_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        // Set up the security context to return the authentication object
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Set the security context in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/registration/sendVerificationToken");
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("success", result);
        verify(eventListener, times(1)).sendNewVerificationEmail(eq(user), anyString());

    }

    @Test
    public void testProcessRequestAndSendEmail_ResendVerificationEmail_EmailVerified() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);
        // Set up the security context to return the authentication object
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Set the security context in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/registration/sendVerificationToken");
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("email_verified", result);
        verify(eventListener, times(0)).sendNewVerificationEmail(eq(user), anyString());

    }


    @Test
    public void testProcessRequestAndSendEmail_ResendVerificationEmail_ErrorSendingVerificationEmail() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        // Set up the security context to return the authentication object
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Set the security context in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/registration/sendVerificationToken");

        doThrow(MessagingException.class).when(eventListener).sendNewVerificationEmail(any(User.class), anyString());
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("error", result);

    }

    @Test
    public void testProcessRequestAndSendEmail_ChangePassword_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username"); // Set the authenticated user's name
        when(authentication.isAuthenticated()).thenReturn(true); // Set authentication to be authenticated
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/changePassword");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("success", result);
        verify(eventListener, times(1)).sendChangePasswordRequestEmail(eq(user), anyString());
    }

    @Test
    public void testProcessRequestAndSendEmail_ChangePassword_ErrorSendingVerificationEmail() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username"); // Set the authenticated user's name
        when(authentication.isAuthenticated()).thenReturn(true); // Set authentication to be authenticated
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/changePassword");

        doThrow(MessagingException.class).when(eventListener).sendChangePasswordRequestEmail(any(User.class), anyString());
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("error", result);

    }

    @Test
    public void testProcessRequestAndSendEmail_ForgotPassword_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/forgotPassword/send-email");

        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("success", result);
        verify(eventListener, times(1)).sendForgotPasswordRequestEmail(eq(user), anyString());
    }

    @Test
    public void testProcessRequestAndSendEmail_ForgotPassword_AccountLocked() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setAccountNonLocked(false);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/forgotPassword/send-email");
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        // Assert
        assertEquals("account_locked", result);
        System.out.println("Result: " + result);
        // Verify that no forgotten password email is sent to the user
        verify(eventListener, never()).sendForgotPasswordRequestEmail(eq(user), anyString());
    }

    @Test
    public void testProcessRequestAndSendEmail_ForgotPassword_ErrorSendingVerificationEmail() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username"); // Set the authenticated user's name
        when(authentication.isAuthenticated()).thenReturn(true); // Set authentication to be authenticated
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/system/forgotPassword/send-email");

        doThrow(MessagingException.class).when(eventListener).sendForgotPasswordRequestEmail(any(User.class), anyString());
        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("error", result);

    }

    @Test
    public void testProcessRequestAndSendEmail_NullReturned() {
        // Arrange
        String email = "test@example.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username"); // Set the authenticated user's name
        when(authentication.isAuthenticated()).thenReturn(true); // Set authentication to be authenticated
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        // Create a mock HttpServletRequest object with a dummy URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/system"));
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/some/invalid/path");

        // Act
        String result = forgotPasswordServiceImpl.processRequestAndSendEmail(email, request);
        System.out.println("Result: " + result);
        // Assert
        assertNull(result);
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "validToken";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setRequestTime(LocalDateTime.now().plusHours(1));

        when(forgotPasswordRepository.findByToken(token)).thenReturn(Optional.of(forgotPasswordToken));

        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, newPassword, confirmPassword, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("change_pw_success", result);
        verify(eventListener, times(1)).sendPasswordSuccessfullyChangedEmail(eq(user));
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_EmptyUser() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "invalidToken";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        when(forgotPasswordRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, null, null, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("empty_user", result);
        verify(eventListener, times(0)).sendPasswordSuccessfullyChangedEmail(any());
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_PasswordError() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "valid_Token";
        String newPassword = "newPassword";
        String confirmPassword = "badNewPassword";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setRequestTime(LocalDateTime.now().plusHours(1));

        when(forgotPasswordRepository.findByToken(token)).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, newPassword, confirmPassword, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("pw_error", result);
        verify(eventListener, times(0)).sendPasswordSuccessfullyChangedEmail(eq(user));
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_ExpiredToken() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "expired_pw_token";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setRequestTime(LocalDateTime.now().minusHours(1));

        when(forgotPasswordRepository.findByToken(token)).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, newPassword, confirmPassword, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("expired_pw_token", result);
        verify(eventListener, times(0)).sendPasswordSuccessfullyChangedEmail(eq(user));
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_InvalidToken() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "invalid_pw_token";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setValid(false);
        forgotPasswordToken.setRequestTime(LocalDateTime.now().plusHours(1));

        when(forgotPasswordRepository.findByToken(anyString())).thenReturn(Optional.of(forgotPasswordToken));
        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, newPassword, confirmPassword, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("invalid_pw_token", result);
        verify(eventListener, times(0)).sendPasswordSuccessfullyChangedEmail(eq(user));
    }

    @Test
    public void testProcessPasswordChangeWithTokenValidation_ErrorSendingVerificationEmail() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "valid_pw_token";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setRequestTime(LocalDateTime.now().plusHours(1));

        when(forgotPasswordRepository.findByToken(anyString())).thenReturn(Optional.of(forgotPasswordToken));
        doThrow(MessagingException.class).when(eventListener).sendPasswordSuccessfullyChangedEmail(any(User.class));
        // Act
        String result = forgotPasswordServiceImpl.processPasswordChangeWithTokenValidation(token, null, newPassword, confirmPassword, request, response);
        System.out.println("Result: " + result);
        // Assert
        assertEquals("email_send_error", result);
        verify(eventListener, times(1)).sendPasswordSuccessfullyChangedEmail(any());
    }

}