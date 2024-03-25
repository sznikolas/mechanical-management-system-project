package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.CompanyNotFoundException;
import com.nikolas.mechanicalmanagementsystem.service.CompanyService;
import com.nikolas.mechanicalmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CompanyController {

    private final UserService userService;
    private final CompanyService companyService;


    //get all companies by user
    @GetMapping("/company")
    public String getAllCompanies(Model model) {
        User user = userService.getLoggedInUser();
        List<Company> companies = companyService.getAllCompaniesByUser(user);
        model.addAttribute("companies", companies);
        return "company";
    }

    //create Company object to hold company form data
    @GetMapping("/company/new")
    public String createCompanyForm(Model model) {
        model.addAttribute("company", new Company());
        return "create_company";
    }

    // save the new company
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/company/create")
    public String createCompany(@ModelAttribute("company") Company company) {
        try {
            User user = userService.getLoggedInUser();
            companyService.createCompany(company, user);
            return "redirect:/company";
        } catch (Exception e) {
            return "redirect:/error?createCompanyError";
        }
    }

    // get company information's/details
    @GetMapping("/company/{companyId}")
    public String getCompanyDetails(@PathVariable Long companyId, Model model) {
        try {
            User loggedInUser = userService.getLoggedInUser();
            Company company = companyService.findById(companyId);
            companyService.checkAccessRightsToOneCompany(loggedInUser, company);
            //we can improve code with DTO!!!
            boolean isLeaderOrDeputyLeader = companyService.isLeaderOrDeputyLeader(loggedInUser, company);
            Set<JobApplication> jobApplications = companyService.getJobApplicationsByCompanyId(companyId);
            Set<User> employeesExceptCurrentUserAndOwner = companyService.getAllEmployeesExceptCurrentUserAndOwner(loggedInUser, companyId);
            Set<User> allEmployees = companyService.getEmployeesByCompanyId(companyId);

            model.addAttribute("isLeaderOrDeputyLeader", isLeaderOrDeputyLeader);
            model.addAttribute("companyDetails", company);
            model.addAttribute("applications", jobApplications);
            model.addAttribute("employeesExceptCurrentUserAndOwner", employeesExceptCurrentUserAndOwner);
            model.addAttribute("allEmployees", allEmployees);

            log.info("CompanyController::getCompanyDetails - Opened company: {} by: {}!", company.getCompanyName() + ' ' + company.getCountry(),
                    loggedInUser.getFirstName() + ' ' + loggedInUser.getLastName());

            return "companyDetails";

        } catch (CompanyNotFoundException e) {
            log.error("CompanyController::getCompanyDetails - Company not found with id: " + companyId);
            return "redirect:/error?companyNotFound";
        } catch (AccessDeniedException e) {
            log.error("CompanyController::getCompanyDetails - No access to the company, ID: {}", companyId);
            return "redirect:/accessDenied";
        } catch (Exception e) {
            log.error("CompanyController::getCompanyDetails - Error while retrieving company details: " + e.getMessage(), e);
            return "error";
        }
    }

    // Delete Company
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/company/deleteCompany/{companyId}")
    public String deleteCompanyById(@PathVariable Long companyId) {
        try {
            companyService.deleteCompanyById(companyId);
            return "redirect:/company?successfullyDeleted";
        } catch (RuntimeException e) {
            return "redirect:/company?deleteFailed";
        }
    }


    //Update Company from the view form
    @PostMapping("/company/update/{companyId}")
    public String updateCompanyDetails(@PathVariable Long companyId, @ModelAttribute("companyDetails") @Valid Company company, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/error?companyUpdateError";
        } else {
            companyService.updateCompany(company);
            return "redirect:/company/" + companyId;
        }
    }

    // Updating the company's deputy leader
    @PostMapping("/company/updateCompanyDeputyLeader/{companyId}")
    public String updateCompanyDeputyLeader(@PathVariable Long companyId, @RequestParam(required = false) Long deputyLeaderId) {
        try {
            companyService.updateCompanyDeputyLeader(companyId, deputyLeaderId);
            return "redirect:/company/" + companyId;
        } catch (Exception e) {
            return "redirect:/error?companyUpdateError";
        }
    }

    // Accept user application
    @PostMapping("/company/accept/{companyId}")
    public String acceptApplication(@PathVariable Long companyId, @RequestParam Long jobApplicationId) {
        companyService.acceptJobApplication(jobApplicationId);
        return "redirect:/company/" + companyId + "?jobApplicationAcceptedSuccessfully";
    }

    // Reject user application
    @PostMapping("/company/reject/{companyId}")
    public String rejectApplication(@PathVariable Long companyId, @RequestParam Long jobApplicationId) {
        companyService.rejectJobApplication(jobApplicationId);
        return "redirect:/company/" + companyId + "?jobApplicationRejectedSuccessfully";
    }

    //    Dismiss employee from company
    //    @Secured("ROLE_ADMIN")
    //    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/company/removeEmployee")
    public String removeEmployeeFromCompany(@RequestParam Long companyId, @RequestParam Long employeeId) {
        companyService.removeEmployeeFromCompany(companyId, employeeId);
        return "redirect:/company/" + companyId + "?EmployeeFiredSuccessfully";
    }

}
