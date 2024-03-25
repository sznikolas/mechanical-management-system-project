package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.service.RoleService;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class AdminController {

    private final RoleService roleService;
    private final UserService userService;

    //Get all user
    @GetMapping
    public String getUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    //Get all user except me
//    @GetMapping
//    public String getUsers(Model model) {
//        User user = userService.getLoggedInUser();
//        model.addAttribute("users", userService.getAllUsersExceptCurrentUser(user));
//        return "users";
//    }

    //Get one user view form
    @GetMapping("/view/{id}")
    public String getUserDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        model.addAttribute("userDetails", user);
        model.addAttribute("defaultRoles", roleService.findAll());
        return "user_details";
    }


    //Update User Roles
    @PostMapping("/updateRoles/{userId}")
    public String updateRoles(@PathVariable("userId") Long userId, @RequestParam("selectedRoles") List<String> selectedRoles) {
        userService.updateUserRoles(userId, selectedRoles);
        return "redirect:/users/view/" + userId;
    }

    //Delete User
    @GetMapping("/deleteUser/{userId}")
    public String deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return "redirect:/users";
    }

    //Toggle User isActive status (ban)
    @PostMapping("/view/toggleIsAccountNonLocked/{userId}")
    public String isActive(@PathVariable("userId") Long userId, @ModelAttribute("userDetails") User userDetails) {
        userService.toggleIsAccountNonLocked(userId);
        return "redirect:/users/view/" + userId;
    }
}

