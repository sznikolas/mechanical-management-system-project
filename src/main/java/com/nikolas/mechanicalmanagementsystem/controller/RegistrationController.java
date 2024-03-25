package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.registration.RegistrationRequest;
import com.nikolas.mechanicalmanagementsystem.registration.emailVerification.EmailVerificationService;
import com.nikolas.mechanicalmanagementsystem.registration.emailVerification.EmailVerificationToken;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService;

    //registration form
    @GetMapping("/registration-form")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationRequest());
        return "registration";
    }

    //registration request
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registration") @Valid RegistrationRequest registration,
                               BindingResult result, HttpServletRequest request) {
        String registrationResult =  userService.handleRegistrationRequest(registration, result, request);
        return "redirect:/registration/registration-form?" + registrationResult;
    }

    //verify email
    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        EmailVerificationToken theToken = emailVerificationService.findByToken(token)
                .orElse(null);
        if(theToken != null && theToken.getUser().isEnabled()){
            return "redirect:/login?verified";
        }
        String verificationResult = emailVerificationService.checkAndValidateRegistration(token);
        return "redirect:/login?" + verificationResult;
    }

    //    Get new verification token page
    @GetMapping("/verificationToken")
    public String getResendVerificationTokenPage() {
        return "resendVerificationToken";
    }

    //new verification token request
    @PostMapping("/sendVerificationToken")
    public String resendVerificationToken(HttpServletRequest request) {
        String email = request.getParameter("email");
        String result = emailVerificationService.processRequestAndSendEmail(email,request);
        return "redirect:/registration/verificationToken?" + result;
    }
}