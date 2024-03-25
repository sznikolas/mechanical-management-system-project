package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Role;
import com.nikolas.mechanicalmanagementsystem.entity.UserRoleEnum;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    List<Role> findAll();
    Role getRoleByName(String roleName);
    Role getOrCreateNewRole(UserRoleEnum userRoleEnum);
}
