package com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceImplTest {
    @InjectMocks
    private ChangePasswordServiceImpl changePasswordServiceImpl;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityContextLogoutHandler securityContextLogoutHandler;
    @Mock
    private ChangePasswordRepository changePasswordRepository;

    @Test
    void testValidatePasswordChange() {
        // Arrange
        User user = new User();
        user.setPassword("existingEncodedPassword");
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        // Act
        boolean result = changePasswordServiceImpl.validatePasswordChange(user, oldPassword, newPassword, confirmPassword);
        // Assert
        assertTrue(result);
        verify(passwordEncoder).matches(eq(oldPassword), eq(user.getPassword()));
    }

    @Test
    void testValidatePasswordChange_WithIncorrectOldPassword() {
        // Arrange
        User user = new User();
        user.setPassword("existingEncodedPassword");
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        // Act
        boolean result = changePasswordServiceImpl.validatePasswordChange(user, oldPassword, newPassword, confirmPassword);
        // Assert
        assertFalse(result);
        // Verify that passwordEncoder's matches method was called with the expected parameters
        verify(passwordEncoder).matches(eq(oldPassword), eq(user.getPassword()));
    }

    @Test
    void testValidatePasswordChange_WithNonMatchingPasswords() {
        // Arrange
        User user = new User();
        user.setPassword("existingEncodedPassword");
        String oldPassword = "existingPassword";
        String newPassword = "newPassword";
        String confirmPassword = "differentPassword";
        // Act
        boolean result = changePasswordServiceImpl.validatePasswordChange(user, oldPassword, newPassword, confirmPassword);
        // Assert
        assertFalse(result);
    }

    @Test
    void testLogoutUser() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        // Act
        changePasswordServiceImpl.logoutUser(request, response);
        // Assert
        // Verify that the logout method of SecurityContextLogoutHandler is called with the correct arguments
        verify(securityContextLogoutHandler).logout(eq(request), eq(response), any());
    }

    @Test
    void testGetTokenRepository() {
        // Act
        JpaRepository<ChangePasswordToken, Long> tokenRepository = changePasswordServiceImpl.getTokenRepository();
        // Assert
        assertEquals(changePasswordRepository, tokenRepository);
    }

    @Test
    void testCreateTokenInstance() {
        // Arrange
        String token = "dummyToken";
        User user = Mockito.mock(User.class);
        // Mock behavior of the User object
        when(user.getFirstName()).thenReturn("testUser");
        // Act
        ChangePasswordToken createdToken = changePasswordServiceImpl.createTokenInstance(user, token);
        System.out.println("token: " + createdToken.getToken());
        // Assert
        assertEquals(token, createdToken.getToken());
        assertEquals(user.getFirstName(), createdToken.getUser().getFirstName());
    }




}