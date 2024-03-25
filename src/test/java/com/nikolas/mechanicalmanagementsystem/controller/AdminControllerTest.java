package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.service.RoleService;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @InjectMocks
    private AdminController adminController;
    @Mock
    private UserService userService;
    @Mock
    private RoleService roleService;
    @Mock
    private Model model = new ConcurrentModel();



    @Test
    void testGetUserDetails() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John Doe");

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.findAll()).thenReturn(List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER")));

        Model model = new ConcurrentModel();
        String viewName = adminController.getUserDetails(1L, model);

        assertEquals("user_details", viewName);
        assertEquals(user, model.getAttribute("userDetails"));
        assertEquals(List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER")), model.getAttribute("defaultRoles"));
    }

    @Test
    void testGetUserDetailsNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> adminController.getUserDetails(1L, model));
    }

}