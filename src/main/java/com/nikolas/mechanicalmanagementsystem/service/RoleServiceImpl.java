package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.UserRoleEnum;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    /**
     * Retrieves a role by its name from the database.
     * If the role with the specified name is found, it is returned.
     * If the role is not found, a NotFoundException is thrown.
     *
     * @param roleName The name of the role to retrieve.
     * @return The role with the specified name, if found.
     * @throws NotFoundException If the role with the specified name is not found.
     */
    public Role getRoleByName(String roleName) {
            // Attempt to find the role by its name in the database
            return roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new NotFoundException("User ROLE not found: " + roleName));
    }


    /**
     * Retrieves an existing role from the database based on the provided UserRoleEnum,
     * or creates a new one if it does not exist.
     *
     * <p><strong>Note:</strong> This method is currently not in use and may be subject to change in future versions.
     * The {@code userRoleEnum} parameter represents the enumeration of the user role,
     * which can be modified according to requirements.
     *
     * @param userRoleEnum The enumeration representing the user role.
     * @return The existing or newly created role.
     */
    @Override
    public Role getOrCreateNewRole(UserRoleEnum userRoleEnum) {
        String roleName = userRoleEnum.getUserRoleEnumName();
        // Attempt to find the available roles by name
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    // If the role does not exist, create a new one
                    Role newRole = new Role(roleName);
                    log.info("RoleServiceImpl::getOrCreateNewRole - New role: {} created", newRole.getRoleName());
                    // Create the new role, save it to the database, and return it
                    return roleRepository.save(newRole);
                });
    }
}