package com.nikolas.mechanicalmanagementsystem.registration.emailVerification;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.utility.EmailSender;
import com.nikolas.mechanicalmanagementsystem.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationServiceImpl;
    @Mock
    private User user;
    @Mock
    EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailSender emailSender;
    @Mock
    private UserRepository userRepository;


    @Test
    public void testCreateTokenInstance() {
        EmailVerificationToken token = emailVerificationServiceImpl.createTokenInstance(user, "dummyToken");
        System.out.println("Token: " + token.getToken());

        assertNotNull(token);
        assertEquals("dummyToken", token.getToken());
        assertEquals(user, token.getUser());
    }

    @Test
    public void testGetTokenRepository() {
        JpaRepository<EmailVerificationToken, Long> emailVerificationTokenRepository = emailVerificationServiceImpl.getTokenRepository();
        assertEquals(emailVerificationRepository, emailVerificationTokenRepository);
    }

    @Test
    public void testValidatePasswordChange() {
        User mockUser = mock(User.class);
        boolean result = emailVerificationServiceImpl.validatePasswordChange(mockUser, "oldPassword",
                "newPassword", "confirmPassword");
        assertFalse(result);
    }




    @Test
    public void testFindByToken() {
        EmailVerificationToken expectedToken = new EmailVerificationToken("dummyToken", new User());

        when(emailVerificationRepository.findByToken("dummyToken")).thenReturn(Optional.of(expectedToken));

        Optional<EmailVerificationToken> result = emailVerificationServiceImpl.findByToken("dummyToken");
        assertTrue(result.isPresent());
        assertEquals(expectedToken, result.get());
    }

    @Test
    public void testFindByToken_NotFound() {
        when(emailVerificationRepository.findByToken("nonExistingToken")).thenReturn(Optional.empty());
        Optional<EmailVerificationToken> result = emailVerificationServiceImpl.findByToken("nonExistingToken");

        assertFalse(result.isPresent());
    }

    @Test
    public void testCheckAndValidateRegistration_InvalidToken() {
        // Arrange
        String token = "invalidToken";
        when(emailVerificationRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        String result = emailVerificationServiceImpl.checkAndValidateRegistration(token);

        // Assert
        assertEquals("invalid", result);
    }

    @Test
    public void testCheckAndValidateRegistration_ExpiredToken() {
        // Arrange
        String token = "expiredToken";
        EmailVerificationToken expiredToken = new EmailVerificationToken();
        expiredToken.setValid(true);
        expiredToken.setRequestTime(LocalDateTime.now().minusDays(1)); // Expired token
        when(emailVerificationRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        // Act
        String result = emailVerificationServiceImpl.checkAndValidateRegistration(token);

        // Assert
        assertEquals("expired", result);
    }

    @Test
    public void testCheckAndValidateRegistration_ValidToken() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "validToken";
        User user = new User();
        EmailVerificationToken validToken = new EmailVerificationToken();
        validToken.setValid(true);
        validToken.setRequestTime(LocalDateTime.now().plusDays(1)); // Valid token
        validToken.setUser(user);
        when(emailVerificationRepository.findByToken(token)).thenReturn(Optional.of(validToken));

        // Act
        String result = emailVerificationServiceImpl.checkAndValidateRegistration(token);

        // Assert
        assertEquals("valid", result);
        assertTrue(user.isEnabled());
        assertFalse(validToken.isValid());
        verify(userRepository, times(1)).save(user);
        verify(emailVerificationRepository, times(1)).save(validToken);
        verify(emailSender, times(1)).sendEmailVerificationWasSuccessful(user);
    }


    @Test
    public void testCheckAndValidateRegistration_MessagingException() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String token = "validToken";
        User user = new User();
        EmailVerificationToken validToken = new EmailVerificationToken();
        validToken.setValid(true);
        validToken.setRequestTime(LocalDateTime.now().plusDays(1));
        validToken.setUser(user);
        when(emailVerificationRepository.findByToken(token)).thenReturn(Optional.of(validToken));
        doThrow(new MessagingException("Mocked MessagingException")).when(emailSender).sendEmailVerificationWasSuccessful(user);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailVerificationServiceImpl.checkAndValidateRegistration(token));
    }




}