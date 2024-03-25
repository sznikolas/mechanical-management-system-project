package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.AlreadyAppliedException;
import com.nikolas.mechanicalmanagementsystem.exception.JobApplicationNotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;
    private final CompanyService companyService;

    /**
     * Withdraws a job application submitted by the logged-in user.
     *
     * @param jobApplicationId The ID of the job application to be withdrawn.
     * @throws JobApplicationNotFoundException If the job application with the specified ID is not found.
     * @throws AccessDeniedException           If the logged-in user is not the owner of the job application and thus unauthorized to withdraw it.
     */
    @Override
    public void withdrawJobApplication(Long jobApplicationId) {
        // Get the logged-in user
        User user = userService.getLoggedInUser();

        // Retrieve the job application by its ID
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() -> new JobApplicationNotFoundException("Job application doesn't found with id: " + jobApplicationId));

        // Check if the logged-in user is the owner of the job application
        if (jobApplication.getEmployee().equals(user)) {
            // Withdraw the job application as the logged-in user is the owner
            jobApplicationRepository.delete(jobApplication);
            log.info("JobApplicationServiceImpl::withdrawJobApplication - {} withdrew their job application from the company: {}", user.getFirstName() + ' ' + user.getLastName(),
                    jobApplication.getCompany().getCompanyName());
        } else {
            // Log and throw an exception for unauthorized access to withdraw the job application
            log.error("JobApplicationServiceImpl::withdrawJobApplication - Unauthorized access to withdraw the job application");
            throw new AccessDeniedException("Unauthorized access to withdraw the job application");
        }
    }


    /**
     * Retrieves a list of companies that the specified user can join based on existing job applications.
     *
     * @param user The user for whom to retrieve the list of joinable companies.
     * @return A list of companies that the user can join.
     */
    @Override
    public List<Company> getCompaniesICanJoin(User user) {
        // Retrieve all companies
        List<Company> allCompanies = companyService.getAllCompanies();

        // List to store companies that the user can join
        List<Company> companiesToJoin = new ArrayList<>();

        // Iterate through all companies
        for (Company company : allCompanies) {
            // Check if the user already has a job application for the company
            JobApplication existingApplication = findByEmployeeAndCompany(user, company);
            if (existingApplication == null) {
                // If the user doesn't have an existing application, add the company to the list of joinable companies
                companiesToJoin.add(company);
            }
        }
        return companiesToJoin;
    }


    /**
     * Applies the specified user to a company by creating a job application.
     *
     * @param user      The user applying to the company.
     * @param companyId The ID of the company to which the user is applying.
     * @throws AlreadyAppliedException if the user has already applied to the specified company.
     */
    @Override
    public void applyToCompany(User user, Long companyId) {
        // Retrieve the company by its ID
        Company company = companyService.findById(companyId);

        // Check if the user has already applied to the company
        JobApplication existingApplication = findByEmployeeAndCompany(user, company);
        if (existingApplication != null) {
            log.warn("JobApplicationServiceImpl::applyToCompany - The user has already applied to this company.");
            throw new AlreadyAppliedException("The user has already applied to this company.");
        }
        // Create a new job application for the user and the specified company
        JobApplication jobApplication = new JobApplication();
        jobApplication.setUser(user);
        jobApplication.setCompany(company);
        jobApplication.setAccepted(false);
        jobApplicationRepository.save(jobApplication);
        log.info("JobApplicationServiceImpl::applyToCompany - {} applied to company {}", user.getLastName() + ' ' + user.getFirstName(), jobApplication.getCompany().getCompanyName());
    }


//    get my job applications
    @Override
    public List<JobApplication> findByEmployee(User user) {
        return jobApplicationRepository.findByEmployee(user);
    }


    /**
     * Finds a job application by the specified employee (user) and company.
     *
     * @param user    The employee (user) for whom to find the job application.
     * @param company The company for which to find the job application.
     * @return The job application if found, or {@code null} if not found.
     */
    @Override
    public JobApplication findByEmployeeAndCompany(User user, Company company) {
        return jobApplicationRepository.findByEmployeeAndCompany(user, company).orElse(null);
    }

}
