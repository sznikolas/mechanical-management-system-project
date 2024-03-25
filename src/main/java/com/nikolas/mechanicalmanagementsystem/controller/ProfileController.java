package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword.ChangePasswordService;
import com.nikolas.mechanicalmanagementsystem.service.CompanyService;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final CompanyService companyService;
    private final ChangePasswordService changePasswordService;


    //get user details in profile
    @GetMapping("/profile")
    public String showProfile(Model model) {
        User user = userService.getLoggedInUser();
        List<Company> allCompanies = companyService.getAllCompaniesByUser(user);
        model.addAttribute("companies", allCompanies);
        model.addAttribute("user", user);
        return "profile";
    }

    //Update user firstName and lastName
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User userDetails) {
        User loggedInUser = userService.getLoggedInUser();
        userService.updateUserDetails(loggedInUser, userDetails);
        return "redirect:/profile";
    }

    // get user roles - not using rn
//    private Collection<String> getRoles(User user) {
//        return user.getRoles()
//                .stream()
//                .map(Role::getRoleName)
//                .collect(Collectors.toList());
//    }


    //quit from the company
    @PostMapping("/leaveCompany")
    public String leaveCompany(@RequestParam("companyId") Long companyId, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getLoggedInUser();
        try {
            companyService.leaveCompany(user.getId(), companyId, request, response);
            return "redirect:/profile";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    //request pw change
    @PostMapping("/changePassword")
    public String sendEmailToChangePassword(HttpServletRequest request) {
        User user = userService.getLoggedInUser();
        log.info("ProfileController::sendEmailToChangePassword - Email where to sent change pw request:" + user.getEmail());
        changePasswordService.processRequestAndSendEmail(user.getEmail(), request);

        return "redirect:/profile?email_sent";
    }

}
