package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.registration.baseToken.changePassword.ChangePasswordService;
import com.nikolas.mechanicalmanagementsystem.registration.baseToken.forgotPassword.ForgotPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final ForgotPasswordService forgotPasswordService;
    private final ChangePasswordService changePasswordService;

    //    Get forgot password view
    @GetMapping("/send-forgot-password-email-form")
    public String showForgotPasswordEmailForm() {
        return "forgot_password_form";
    }

//   send forgot pw email request
    @PostMapping("/send-email")
    public String changePasswordRequest(HttpServletRequest request) {
        String email = request.getParameter("email");
        String result = forgotPasswordService.processRequestAndSendEmail(email,request);
        return "redirect:/forgotPassword/send-forgot-password-email-form?" + result;
    }

//    change pw form
    @GetMapping("/change-password")
    public String showChangePasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "change_password";
    }

//    send change pw request
    @PostMapping("/change-password")
    public String changePassword(HttpServletRequest request, HttpServletResponse response) {
        String theToken = request.getParameter("token");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        String result = changePasswordService.processPasswordChangeWithTokenValidation(theToken, oldPassword, newPassword, confirmPassword, request, response);

        return "redirect:/forgotPassword/change-password?" + result + "&token=" + theToken;
    }

    // forgot pw form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "forgot_password";
    }
//    send forgot pw request
    @PostMapping("/forgot-password")
    public String forgotPassword(HttpServletRequest request) {
        String theToken = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        String result = forgotPasswordService.processPasswordChangeWithTokenValidation(theToken,null,newPassword,confirmPassword,null,null);

        return "redirect:/forgotPassword/forgot-password?" + result + "&token=" + theToken;
    }
}
