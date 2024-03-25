package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;

import java.util.List;

public interface JobApplicationService {

    void withdrawJobApplication(Long jobApplicationId);
    List<JobApplication> findByEmployee(User user);
    JobApplication findByEmployeeAndCompany(User user, Company company);
    List<Company> getCompaniesICanJoin(User user);
    void applyToCompany(User user, Long companyId);

}
