package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.dtos.CompanyDTO;
import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.AlreadyAppliedException;
import com.nikolas.mechanicalmanagementsystem.service.CompanyService;
import com.nikolas.mechanicalmanagementsystem.service.JobApplicationService;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchAndJoinCompanyController {
    private final UserService userService;
    private final CompanyService companyService;
    private final JobApplicationService jobApplicationService;



    //get all companies
    @GetMapping("/searchAndJoinCompany")
    public String getAllCompaniesToJoin(Model model) {
        User user = userService.getLoggedInUser();

        List<JobApplication> companiesIApplied = jobApplicationService.findByEmployee(user);
        List<Company> companiesICanJoin = jobApplicationService.getCompaniesICanJoin(user);

        model.addAttribute("companiesICanJoin", companiesICanJoin);
        model.addAttribute("userJobApplications", companiesIApplied);
        return "searchAndJoinCompany";
    }

    // Apply to a company
    @PostMapping("/searchAndJoinCompany/apply/{companyId}")
    public String applyToCompany(@PathVariable Long companyId) {
        User user = userService.getLoggedInUser();
        try {
            jobApplicationService.applyToCompany(user,companyId);
            return "redirect:/searchAndJoinCompany?successfullyApplied";
        } catch (AlreadyAppliedException e) {
            // The user is already applied to the company, here we handle
            return "redirect:/searchAndJoinCompany?alreadyApplied";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    // Withdraw job application
    @PostMapping("/withdrawJobApplication/{jobApplicationId}")
    public String withdrawJobApplication(@PathVariable Long jobApplicationId) {
        jobApplicationService.withdrawJobApplication(jobApplicationId);
        return "redirect:/searchAndJoinCompany?successfullyWithdrawn";
    }

    //    get company details in MODAL
    // Javascript passing data to Modal with ResponseBody and DTO to avoid recursion, but it works
    @RequestMapping("/searchAndJoinCompany/{companyId}")
    @ResponseBody
    public CompanyDTO getCompanyDetails(@PathVariable Long companyId) {
        return companyService.getCompanyDetails(companyId);
    }
}
