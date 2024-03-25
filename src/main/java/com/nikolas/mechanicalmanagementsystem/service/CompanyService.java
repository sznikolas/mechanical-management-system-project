package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.CompanyDTO;
import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Set;

public interface CompanyService {

    Company findById(Long id);
    List<Company> getAllCompanies();
    void updateCompany(Company company);
    void updateCompanyDeputyLeader(Long companyId, Long deputyLeaderId);
    void createCompany(Company company, User user);
    void deleteCompanyById(Long companyId);
    List<Company> getAllCompaniesByUser(User user);
    Set<JobApplication> getJobApplicationsByCompanyId(Long companyId);
    Set<User> getEmployeesByCompanyId(Long companyId);
    Set<User> getAllEmployeesExceptCurrentUserAndOwner(User currentUser, Long companyId);
    void acceptJobApplication(Long applicationId);
    void removeEmployeeFromCompany(Long companyId, Long employeeId);
    void rejectJobApplication(Long jobApplicationId);
    void leaveCompany(Long userId, Long companyId, HttpServletRequest request, HttpServletResponse response);
    void addNewMachineToCompany(Machine machine, Long companyId);
    void checkAccessRightsToOneCompany(User loggedInUser, Company company);
    boolean isLeaderOrDeputyLeader(User loggedInUser, Company company);
    CompanyDTO getCompanyDetails(Long companyId);
    List<Long> getSelectedCompaniesByIds(HttpSession session, List<Long> selectedCompanyIds, List<Company> companies);
    Integer getMachinesByMonth(HttpSession session, Integer month);
    Integer getMachinesByYear(HttpSession session, Integer year);
}
