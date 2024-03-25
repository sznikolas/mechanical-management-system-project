package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.UserRoleEnum;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @InjectMocks
    private RoleServiceImpl roleServiceImpl;
    @Mock
    private RoleRepository roleRepository;

    @Test
    void testFindAllRoles() {
        // Arrangement
        List<Role> expectedRoles = new ArrayList<>();
        expectedRoles.add(new Role("ROLE_ADMIN"));
        expectedRoles.add(new Role("ROLE_USER"));
//        expectedRoles.add(new Role("ROLE_EXAMPLE"));

        when(roleRepository.findAll()).thenReturn(expectedRoles);

        // Action
        List<Role> actualRoles = roleServiceImpl.findAll();

        // Assertion
        assertEquals(expectedRoles.size(), actualRoles.size());

        // Check that all roles are included in the expected roles
        Set<String> expectedRoleNames = expectedRoles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        Set<String> actualRoleNames = actualRoles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        System.out.println("Expected role names: " + expectedRoleNames);
        System.out.println("Actual role names: " + actualRoleNames);
        assertTrue(actualRoleNames.containsAll(expectedRoleNames), "Not all expected roles found");
    }

    @Test
    void testGetRoleByName_WhenRoleExists() {
        // Arrangement
        String roleName = "ROLE_USER";
        Role expectedRole = new Role(roleName);
        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.of(expectedRole));

        // Action
        Role foundRole  = roleServiceImpl.getRoleByName(roleName);
        // Assertion
        assertNotNull(foundRole);
        assertEquals(expectedRole, foundRole);
        verify(roleRepository, times(1)).findByRoleName(roleName);
    }

    @Test
    void testGetRoleByName_WhenRoleNotExists() {
        // Arrange
        String roleName = "NON_EXISTING_ROLE";
        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleServiceImpl.getRoleByName(roleName));
        verify(roleRepository, times(1)).findByRoleName(roleName);
    }

    @Test
    void testGetOrCreateNewRole_WhenRoleExists() {
        // Arrange
        String roleName = "USER";
        Role existingRole = new Role(roleName);
        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.of(existingRole));

        RoleServiceImpl roleService = new RoleServiceImpl(roleRepository);

        // Act
        Role result = roleService.getOrCreateNewRole(UserRoleEnum.USER);

        // Assert
        assertEquals(existingRole, result);
        verify(roleRepository, times(1)).findByRoleName(roleName);
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void testGetOrCreateNewRole_WhenRoleDoesNotExist() {
        // Arrange
        String roleName = "USER";

        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0); // The Role object passed as an argument
            role.setId(1L);
            return role;
        });

        // Act
        Role result = roleServiceImpl.getOrCreateNewRole(UserRoleEnum.USER);

        // Assert
        assertNotNull(result);
        assertEquals(roleName, result.getRoleName());
        verify(roleRepository, times(1)).findByRoleName(roleName);
        verify(roleRepository, times(1)).save(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }



}