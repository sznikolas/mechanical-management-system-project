package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.CompanyDTO;
import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.exception.CompanyNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.JobApplicationNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final UserRepository userRepository;
    private final MachineService machineService;

    @Override
    public Company findById(Long companyId) {
        return companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }


    /**
     * Checks if the logged-in user has access rights to the specified company.
     *
     * @param loggedInUser The logged-in user whose access rights are checked.
     * @param company      The company for which access rights are checked.
     * @throws AccessDeniedException if the logged-in user does not have access rights to the company.
     */
    @Override
    public void checkAccessRightsToOneCompany(User loggedInUser, Company company) {
        List<Company> userCompanies = loggedInUser.getRoles().stream()
                .flatMap(role -> role.getCompanyRoles().stream())
                .map(CompanyRole::getCompany)
                .toList();

        if (userCompanies.stream().noneMatch(userCompany -> userCompany.getId().equals(company.getId()))) {
            throw new AccessDeniedException("Access denied to company with id: " + company.getId());
        }
    }


    /**
     * Checks if the logged-in user is the owner or deputy leader of the specified company.
     *
     * @param loggedInUser The logged-in user whose role is checked.
     * @param company      The company for which leadership roles are checked.
     * @return {@code true} if the user is the owner or deputy leader of the company, {@code false} otherwise.
     */
    @Override
    public boolean isLeaderOrDeputyLeader(User loggedInUser, Company company) {
        return company.getOwner().equals(loggedInUser) ||
                (company.getDeputyLeader() != null && company.getDeputyLeader().equals(loggedInUser));
    }


    /**
     * Updates the details of a company in the database.
     *
     * @param company The updated company details.
     * @throws CompanyNotFoundException if the company with the given ID is not found in the database.
     */
    @Transactional
    @Override
    public void updateCompany(Company company) {
        // Retrieve the existing company entity from the database by ID or throw an exception if not found
        Company existingCompany = companyRepository.findById(company.getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + company.getId()));

        // Update the company details with the provided information
        if (company.getOwner() != null) {
            existingCompany.setOwner(company.getOwner());
        }
        existingCompany.setCompanyName(company.getCompanyName());
        existingCompany.setDescription(company.getDescription());
        existingCompany.setCountry(company.getCountry());
        existingCompany.setLocation(company.getLocation());
        existingCompany.setStreet(company.getStreet());
        existingCompany.setPostCode(company.getPostCode());
        existingCompany.setIdentificationNumber(company.getIdentificationNumber());
        existingCompany.setTaxIdentificationNumber(company.getTaxIdentificationNumber());
        existingCompany.setValueAddedTaxIdentificationNumber(company.getValueAddedTaxIdentificationNumber());
        log.info("CompanyServiceImpl::updateCompany - Company details: {} updated successfully!", company.getCompanyName() + ' ' + company.getCountry());

        companyRepository.save(existingCompany);
    }


    /**
     * Updates the deputy leader of a company in the database.
     *
     * @param companyId      The ID of the company whose deputy leader is to be updated.
     * @param deputyLeaderId The ID of the user to be set as the new deputy leader.
     *                       Pass null to remove the deputy leader.
     * @throws CompanyNotFoundException if the company with the given ID is not found in the database.
     * @throws NotFoundException        if the user with the given deputy leader ID is not found in the database.
     */
    @Transactional
    @Override
    public void updateCompanyDeputyLeader(Long companyId, Long deputyLeaderId) {
        // Retrieve the company entity from the database by ID or throw an exception if not found
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company doesn't exist with id: " + companyId));
        try {
            if (deputyLeaderId != null) {
                // Retrieve the deputy leader user entity from the database by ID or throw an exception if not found
                User deputyLeader = userRepository.findById(deputyLeaderId).orElseThrow(() -> new NotFoundException("Deputy leader not found with id: " + deputyLeaderId));

                // Set the deputy leader of the company and save the changes in the database
                company.setDeputyLeader(deputyLeader);
                companyRepository.save(company);
                log.info("CompanyServiceImpl::updateCompanyDeputyLeader - The new deputy leader of the company {} is {}!", company.getCompanyName(), deputyLeader.getFirstName() + ' ' + deputyLeader.getLastName());
            } else {
                // Remove the deputy leader from the company and save the changes in the database
                company.setDeputyLeader(null);
                companyRepository.save(company);
                log.info("CompanyServiceImpl::updateCompanyDeputyLeader - The new deputy leader of the company {} is EMPTY!", company.getCompanyName());
            }
        } catch (Exception e) {
            // Log and rethrow any unexpected exceptions
            log.error("CompanyServiceImpl::updateCompanyDeputyLeader - An error occurred while updating the company deputy leader: " + e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Creates a new company in the database and sets up the necessary connections with the owner and roles.
     *
     * @param company The company entity to be created.
     * @param user    The details of the user creating the company.
     * @throws NotFoundException if the logged-in user is not found.
     */
    @Transactional
    @Override
    public void createCompany(Company company, User user) {
        String myEmail = user.getEmail();
        User owner = userService.findByEmail(myEmail)
                .orElseThrow(() -> new NotFoundException("Logged in user not found"));
        try {
            // Set up the owner and deputy leader of the company
            company.setOwner(owner);
            company.setDeputyLeader(owner);
            companyRepository.save(company);

            // Create a unique role name based on company data
            String roleName = company.getId() + "_" + company.getCompanyName().toUpperCase() + '_' + company.getCountry().toUpperCase();
            // Create a new role entity with the generated role name and save it
            Role role = new Role(roleName);
            roleRepository.save(role);

            // Assign the role to the owner user and save the user
            owner.getRoles().add(role);
            userService.saveUser(owner);

            // Set up the connection between the company and the role
            CompanyRole companyRole = new CompanyRole();
            companyRole.setCompany(company);
            companyRole.setRole(role);
            companyRoleRepository.save(companyRole);

            log.info("CompanyServiceImpl::createCompany - Company {} is created with company role {}, by {}!", company.getCompanyName(), roleName, user.getFirstName() + ' ' + user.getLastName());
        } catch (Exception e) {
            log.error("CompanyServiceImpl::createCompany - Unexpected error occurred while creating company", e);
            throw new RuntimeException("Unexpected error occurred while creating company", e);
        }
    }


    /**
     * Deletes a company and all associated data from the system based on the provided company ID.
     *
     * @param companyId The ID of the company to be deleted.
     * @throws CompanyNotFoundException if the company with the provided ID is not found.
     */
    @Transactional
    @Override
    public void deleteCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company doesn't exist with id: " + companyId));
        try {
            // Get the company roles to delete
            Set<CompanyRole> companyRolesToDelete = company.getCompanyRoles();
            // Get the IDs of the company roles for further processing
            Set<Long> roleIdsToDeleteFromEmployee = companyRoleIds(company);
            // Retrieve the Role objects corresponding to the role IDs
            List<Role> rolesToDelete = roleRepository.findAllById(roleIdsToDeleteFromEmployee);

            // Delete associated job applications, user roles, company roles, roles, and finally the company
            jobApplicationRepository.deleteAll(company.getJobApplications());
            userService.removeRolesFromUsers(rolesToDelete);
            companyRoleRepository.deleteAll(companyRolesToDelete);
            roleRepository.deleteAll(rolesToDelete);
            companyRepository.delete(company);
            log.info("CompanyServiceImpl::deleteCompanyById - Company {} is deleted from the system!", company.getCompanyName() + ' ' + company.getCountry());

        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting company");
            throw new RuntimeException("Unexpected error occurred while creating company", e);
        }
    }


    /**
     * Retrieves all companies associated with the provided user.
     *
     * @param user The user whose associated companies are to be retrieved.
     * @return A list of companies associated with the user.
     */
    @Override
    public List<Company> getAllCompaniesByUser(User user) {
        String userEmail = user.getEmail();
        // Extract user authorities and collect them into a set
        Set<String> userAuthorities = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        List<Company> companies = new ArrayList<>();
        try {
            if (userAuthorities.contains("ADMIN")) {
                // User with admin privileges
                companies = companyRepository.findAll();
                log.info("CompanyServiceImpl::getAllCompaniesByUser - All companies retrieved by admin user: {}", userEmail);
            } else {
                // User without admin privileges
                companies = userService.getWorkplacesForLoggedInUser(userEmail);
                log.info("CompanyServiceImpl::getAllCompaniesByUser - Companies retrieved for user: {}", userEmail);
            }
        } catch (Exception e) {
            // Log any errors that occur during the retrieval process
            log.error("CompanyServiceImpl::getAllCompaniesByUser - Error occurred while retrieving companies for user: {}", userEmail, e);
        }
        return companies;
    }


    /**
     * Retrieves the set of job applications associated with the provided company ID.
     *
     * @param companyId The ID of the company whose job applications are to be retrieved.
     * @return The set of job applications associated with the company.
     * @throws CompanyNotFoundException if no company is found with the provided ID.
     */
    @Override
    public Set<JobApplication> getJobApplicationsByCompanyId(Long companyId) {
        // Retrieve the company by ID from the repository
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));

        // Filter the job applications to include only pending ones (not yet accepted)
        return company.getJobApplications()
                .stream()
                .filter(application -> !application.isAccepted())
                .collect(Collectors.toSet());
    }


    /**
     * Retrieves the set of employees associated with the provided company ID.
     *
     * @param companyId The ID of the company whose employees are to be retrieved.
     * @return The set of employees associated with the company.
     * @throws CompanyNotFoundException if no company is found with the provided ID.
     */
    @Override
    public Set<User> getEmployeesByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));
        return company.getEmployees();
    }


    /**
     * Retrieves all employees associated with the provided company ID, excluding the current user and the owner of the company.
     *
     * @param currentUser The current user for whom the employees are being retrieved.
     * @param companyId   The ID of the company whose employees are to be retrieved.
     * @return The set of employees associated with the company, excluding the current user and the owner.
     * @throws CompanyNotFoundException if no company is found with the provided ID.
     */
    @Override
    public Set<User> getAllEmployeesExceptCurrentUserAndOwner(User currentUser, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));
        Set<User> companyEmployees = new HashSet<>(company.getEmployees());

        // Remove the current user from the list
        companyEmployees.removeIf(user -> user.equals(currentUser));

        // Remove the company owner from the list if available
        User owner = company.getOwner();
        if (owner != null) {
            companyEmployees.removeIf(user -> user.getId().equals(owner.getId()));
        }

        return companyEmployees;
    }


    /**
     * Accepts a job application by setting its status to accepted and adding the applicant to the company's employees.
     * It also assigns appropriate roles to the newly recruited employee.
     *
     * @param jobApplicationId The ID of the job application to accept.
     * @throws JobApplicationNotFoundException if no job application is found with the provided ID.
     */
    @Override
    @Transactional
    public void acceptJobApplication(Long jobApplicationId) {
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() -> new JobApplicationNotFoundException("Job application not found with id: " + jobApplicationId));

        jobApplication.setAccepted(true);

        Company company = jobApplication.getCompany();
        User applicant = jobApplication.getEmployee();

        // Get the company role's IDs
        Set<Long> roleIdsAddToEmployee = companyRoleIds(company);
        // Query the corresponding Role objects by roleIdsAddToEmployee
        List<Role> rolesAddToEmployee = roleRepository.findAllById(roleIdsAddToEmployee);

        // Add the accepted candidate to the company employees
        company.getEmployees().add(applicant);
        // Assign appropriate roles to the newly recruited employee
        applicant.getRoles().addAll(rolesAddToEmployee);

        // save to DB
        jobApplicationRepository.save(jobApplication);
        companyRepository.save(company);

        log.info("CompanyServiceImpl::acceptJobApplication - {} has been successfully recruited to the company {}!",
                applicant.getFirstName() + ' ' + applicant.getLastName(), company.getCompanyName());
    }


    /**
     * Retrieves the IDs of company roles associated with the given company.
     *
     * @param company The company for which to retrieve role IDs.
     * @return A set containing the IDs of company roles.
     */
    private static Set<Long> companyRoleIds(Company company) {
        return company.getCompanyRoles().stream()
                .map(companyRole -> companyRole.getRole().getId())
                .collect(Collectors.toSet());
    }


    /**
     * Rejects a job application by deleting it from the repository.
     *
     * @param jobApplicationId The ID of the job application to reject.
     * @throws JobApplicationNotFoundException if the job application with the specified ID is not found.
     */
    @Override
    @Transactional
    public void rejectJobApplication(Long jobApplicationId) {
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() -> new JobApplicationNotFoundException("Job application not found with id: " + jobApplicationId));

        jobApplicationRepository.delete(jobApplication);
        log.info("CompanyServiceImpl::rejectJobApplication - Job application rejected: {} for COMPANY: {}", jobApplication.getEmployee().getFirstName() + ' ' + jobApplication.getEmployee().getLastName(),
                jobApplication.getCompany().getCompanyName());
    }


    /**
     * Removes an employee from a company by deleting their roles, removing them from the company's employee list,
     * and deleting their job applications for the company.
     *
     * @param companyId  The ID of the company from which the employee is to be removed.
     * @param employeeId The ID of the employee to be removed.
     * @throws CompanyNotFoundException if the company with the specified ID is not found.
     * @throws NotFoundException        if the employee with the specified ID is not found.
     */
    @Override
    @Transactional
    public void removeEmployeeFromCompany(Long companyId, Long employeeId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));
        User user = userService.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + employeeId));

        Set<User> employees = getEmployeesByCompanyId(companyId);

        if (!employees.contains(user)) {
            log.error("Employee with id {} is not part of the company with id {}", employeeId, companyId);
            return;
        }

        // Get the company role's IDs
        Set<Long> roleIdsToDeleteFromEmployee = companyRoleIds(company);
        // Query the corresponding Role objects by roleIdsToDeleteFromEmployee
        List<Role> rolesToDelete = roleRepository.findAllById(roleIdsToDeleteFromEmployee);
        // Remove roles from the employee
        rolesToDelete.forEach(user.getRoles()::remove);
        // Remove the employee from the company's employee list
        employees.remove(user);

        companyRepository.save(company);
        // Delete job applications for the removed employee and company
        deleteJobApplicationsForCompanyAndUser(user, company);

        log.info("CompanyServiceImpl::removeEmployeeFromCompany - {} dismissed from the COMPANY {}!", user.getFirstName() + ' ' + user.getLastName(), company.getCompanyName());
    }


    /**
     * Deletes all job applications associated with a specific user and company.
     *
     * @param user    The user for whom job applications are to be deleted.
     * @param company The company for which job applications are to be deleted.
     */
    private void deleteJobApplicationsForCompanyAndUser(User user, Company company) {
        jobApplicationRepository.deleteAllByEmployeeAndCompany(user, company);
    }


    /**
     * Allows a user to leave a company, removing them from the company's employees and deleting associated roles and job applications.
     * If the user leaving is the owner or deputy leader of the company, a new owner and deputy leader (admin) will be assigned.
     *
     * @param userId    The ID of the user leaving the company.
     * @param companyId The ID of the company the user is leaving.
     * @param request   The HTTP servlet request.
     * @param response  The HTTP servlet response.
     */
    @Transactional
    @Override
    public void leaveCompany(Long userId, Long companyId, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + userId));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + companyId));
        // Get the role IDs to delete from the employee
        Set<Long> roleIdsToDeleteFromEmployee = companyRoleIds(company);
        List<Role> rolesToDelete = roleRepository.findAllById(roleIdsToDeleteFromEmployee);
        // Remove roles from the user
        rolesToDelete.forEach(user.getRoles()::remove);
        // Remove user from company
        company.getEmployees().remove(user);
        // Delete job applications for the user and company
        deleteJobApplicationsForCompanyAndUser(user, company);
        // If the user is the owner or deputy leader, assign a new owner and deputy leader
        if (user == company.getOwner() || user == company.getDeputyLeader()) {
            User newOwner = userRepository.findByEmail("admin@gmail.com")
                    .orElseThrow(() -> new NotFoundException("Email not found"));
            company.setOwner(newOwner);
            company.setDeputyLeader(newOwner);
            log.info("CompanyServiceImpl::leaveCompany - Deputy leader was {} and left the COMPANY: {}, the new deputy leader is: {}!", user.getFirstName() + ' ' + user.getLastName(),
                    company.getCompanyName(), newOwner.getFirstName() + ' ' + newOwner.getLastName());
        }

        companyRepository.save(company);
        userRepository.save(user);
        // Logout user
        logoutUser(request, response);
        log.info("CompanyServiceImpl::leaveCompany - {} left the COMPANY: {}!", user.getFirstName() + ' ' + user.getLastName(), company.getCompanyName());
    }


    /**
     * Logs out the currently authenticated user by invalidating the session and clearing authentication details.
     *
     * @param request  The HTTP servlet request.
     * @param response The HTTP servlet response.
     */
    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

    }


    /**
     * Adds a new machine to the company after performing necessary validations.
     *
     * @param machine   The machine to be added.
     * @param companyId The ID of the company to which the machine will be added.
     * @throws NotFoundException     If the logged-in user or company is not found.
     * @throws AccessDeniedException If the logged-in user does not belong to the company or does not have permission to add a machine.
     */
    @Transactional
    @Override
    public void addNewMachineToCompany(Machine machine, Long companyId) {
        User user = userService.getLoggedInUser();
        String email = user.getEmail();
        User loggedInUser = userService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Logged in user not found: " + user.getEmail()));

        Company company = findById(companyId);

        // Check if the logged-in user belongs to the company or is the owner
        if (!company.getEmployees().contains(loggedInUser) && company.getOwner() != loggedInUser) {
            throw new AccessDeniedException("The logged-in user does not belong to this company");
        }
        // Calculate and set the profit for the machine
        machineService.calculateAndSetProfit(machine);
        // Add the machine to the company and save
        addMachineToCompanyAndSave(machine, company);
        // Log the addition of the machine to the company
        log.info("CompanyServiceImpl::addNewMachineToCompany - {} successfully added this MACHINE: {} to this COMPANY: {}!", user.getFirstName() + ' ' + user.getLastName(),
                machine.getMachineBrand() + ' ' + machine.getMachineModel(),
                company.getCompanyName() + ' ' + company.getCountry());
    }


    /**
     * Adds the specified machine to the given company and saves the changes.
     *
     * @param machine The machine to be added to the company.
     * @param company The company to which the machine will be added.
     */
    @Transactional
    public void addMachineToCompanyAndSave(Machine machine, Company company) {
        // Set the company for the machine
        machine.setCompany(company);
        // Add the machine to the company's list of machines
        company.getMachines().add(machine);
        // Save the company to persist the changes
        companyRepository.save(company);
    }


    /**
     * Retrieves and returns the details of the company identified by the provided ID.
     *
     * @param companyId The ID of the company for which details are requested.
     * @return A CompanyDTO object containing the details of the company.
     * @throws CompanyNotFoundException if no company is found with the specified ID.
     */
    @Override
    public CompanyDTO getCompanyDetails(Long companyId) {
        // Retrieve the company by its ID
        Company company = findById(companyId);

        // Create a CompanyDTO object to hold the company details
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setCompanyName(company.getCompanyName());
        companyDTO.setDescription(company.getDescription());
        companyDTO.setCountry(company.getCountry());
        companyDTO.setLocation(company.getLocation());
        companyDTO.setStreet(company.getStreet());
        companyDTO.setPostCode(company.getPostCode());
        companyDTO.setRegistrationDate(company.getRegistrationDate());
        companyDTO.setOwner(company.getOwner().getFirstName() + " " + company.getOwner().getLastName());
        // Set any other settings as needed
        return companyDTO;
    }


    /**
     * Retrieves the IDs of selected companies based on the user's session and selected company IDs.
     * Validates whether the selected company IDs are valid for the user.
     * If not, throws an AccessDeniedException.
     * Initializes the selected companies list in the session if it doesn't exist.
     * Updates the selected companies list in the session if its change.
     *
     * @param session            The HttpSession object to retrieve and store session attributes.
     * @param selectedCompanyIds A List of Long containing the IDs of companies selected by the user.
     * @param userCompanies      A List of Company objects representing all companies associated with the user.
     * @return A List of Long containing the IDs of selected companies.
     * @throws AccessDeniedException If the selected company IDs are not valid for the user.
     */
    @Override
    public List<Long> getSelectedCompaniesByIds(HttpSession session, List<Long> selectedCompanyIds, List<Company> userCompanies) {

        // Retrieve IDs of all companies associated with the user
        List<Long> userAllCompanies = userCompanies.stream()
                .map(Company::getId)
                .collect(Collectors.toList());

        // Validate selected company IDs if provided
        if (selectedCompanyIds != null && !selectedCompanyIds.isEmpty()) {
            boolean hasValidCompanies = new HashSet<>(userAllCompanies).containsAll(selectedCompanyIds);
            if (!hasValidCompanies) {
                throw new AccessDeniedException("User dont have access rights");
            }
        }
        // Retrieve or initialize selected companies list in the session
        List<Long> selectedCompanies = (List<Long>) session.getAttribute("selectedCompanies");
        if (selectedCompanies == null) {
            selectedCompanies = userAllCompanies;
            session.setAttribute("selectedCompanies", selectedCompanies);
            log.info("CompanyServiceImpl::getSelectedCompaniesIds selectedCompanies == null: " + selectedCompanies);
        }
        // Update selected companies list in the session if its change
        if (selectedCompanyIds != null) {
            selectedCompanies = selectedCompanyIds;
            session.setAttribute("selectedCompanies", selectedCompanies);
            log.info("CompanyServiceImpl::getSelectedCompaniesIds selectedCompanies != null: " + selectedCompanies);
        }
        return selectedCompanies;
    }

    /**
     * Retrieves the selected month from the session attribute "selectedMonth".
     * If the selected month is not set in the session, it defaults to the current month.
     * If a new month is provided, updates the selected month in the session.
     *
     * @param session The HttpSession object to retrieve and store session attributes.
     * @param month   An Integer representing the selected month, if provided.
     * @return The selected month. If not set, defaults to the current month.
     */
    @Override
    public Integer getMachinesByMonth(HttpSession session, Integer month) {
        // Retrieve the selected month from the session
        Integer selectedMonth = (Integer) session.getAttribute("selectedMonth");
        // If the selected month is not set in the session, set it to the current month
        if (selectedMonth == null) {
            LocalDate currentDate = LocalDate.now();
            selectedMonth = currentDate.getMonthValue();
            session.setAttribute("selectedMonth", selectedMonth);
        }
        // If a new month is provided, update the selected month in the session
        if (month != null) {
            selectedMonth = month;
            session.setAttribute("selectedMonth", selectedMonth);
        }
        // Return the selected month
        return selectedMonth;
    }

    /**
     * Retrieves the selected year from the user's session attribute or sets it to the current year if not present.
     * Updates the selected year in the session if a new year is provided.
     *
     * @param session The HttpSession object to retrieve and store session attributes.
     * @param year    The selected year provided by the client, if any.
     * @return The selected year.
     */
    @Override
    public Integer getMachinesByYear(HttpSession session, Integer year) {
        Integer selectedYear = (Integer) session.getAttribute("selectedYear");
        if (selectedYear == null) {
            LocalDate currentDate = LocalDate.now();
            selectedYear = currentDate.getYear();
            session.setAttribute("selectedYear", selectedYear);
        }
        if (year != null) {
            selectedYear = year;
            session.setAttribute("selectedYear", selectedYear);
        }
        return selectedYear;
    }
}

