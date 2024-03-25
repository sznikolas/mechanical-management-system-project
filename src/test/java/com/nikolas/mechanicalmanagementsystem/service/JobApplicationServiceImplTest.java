package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import com.nikolas.mechanicalmanagementsystem.exception.AlreadyAppliedException;
import com.nikolas.mechanicalmanagementsystem.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceImplTest {
    @InjectMocks
    private JobApplicationServiceImpl jobApplicationServiceImpl;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private UserService userService;
    @Mock
    private CompanyService companyService;

    private static User user;
    private static Company company1;
    private static JobApplication jobApplication;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        company1 = new Company();
        company1.setCompanyName("Test Company");

        jobApplication = new JobApplication();
        jobApplication.setId(1L);
        jobApplication.setCompany(company1);
        jobApplication.setEmployee(user);

    }

    @Test
    void testWithdrawJobApplication_Success() {
        when(userService.getLoggedInUser()).thenReturn(user);
        when(jobApplicationRepository.findById(jobApplication.getId())).thenReturn(Optional.of(jobApplication));

        // Act
        jobApplicationServiceImpl.withdrawJobApplication(jobApplication.getId());

        // Assert
        verify(userService, times(1)).getLoggedInUser();
        verify(jobApplicationRepository, times(1)).findById(jobApplication.getId());
        verify(jobApplicationRepository, times(1)).delete(jobApplication);
    }

    @Test
    void testWithdrawJobApplication_UnauthorizedAccess() {
        // Arrange
        User unauthorizedUser = new User();

        when(userService.getLoggedInUser()).thenReturn(unauthorizedUser);
        when(jobApplicationRepository.findById(jobApplication.getId())).thenReturn(java.util.Optional.of(jobApplication));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> jobApplicationServiceImpl.withdrawJobApplication(jobApplication.getId()));
        verify(userService, times(1)).getLoggedInUser();
        verify(jobApplicationRepository, times(1)).findById(jobApplication.getId());
        verifyNoMoreInteractions(jobApplicationRepository);
    }

    @Test
    void testGetCompaniesICanJoin() {
        // Arrange
        List<Company> allCompanies = new ArrayList<>();
        Company company2 = new Company();
        allCompanies.add(company1);
        allCompanies.add(company2);

        when(companyService.getAllCompanies()).thenReturn(allCompanies);
        when(jobApplicationServiceImpl.findByEmployeeAndCompany(any(User.class), any(Company.class)))
                .thenAnswer(invocation -> Optional.empty());

        // Act
        List<Company> companiesICanJoin = jobApplicationServiceImpl.getCompaniesICanJoin(user);

        // Assert
        assertEquals(2, companiesICanJoin.size());
        assertTrue(companiesICanJoin.contains(company1));
        assertTrue(companiesICanJoin.contains(company2));
    }

    @Test
    void testApplyToCompany() {
        // Mock companyService findById
        when(companyService.findById(company1.getId())).thenReturn(company1);

        // Mock findByEmployeeAndCompany method to not find existing applications
        when(jobApplicationServiceImpl.findByEmployeeAndCompany(any(User.class), any(Company.class)))
                .thenAnswer(invocation -> Optional.empty());

        // Act
        assertDoesNotThrow(() -> jobApplicationServiceImpl.applyToCompany(user, company1.getId()));
        // Assert
        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    void testApplyToCompany_AlreadyApplied() {
        // Mock companyService findById
        when(companyService.findById(company1.getId())).thenReturn(company1);

        // Mock findByEmployeeAndCompany method to find an existing application
        JobApplication existingApplication = new JobApplication();
        existingApplication.setUser(user);
        existingApplication.setCompany(company1);

        when(jobApplicationServiceImpl.findByEmployeeAndCompany(any(User.class), any(Company.class)))
                .thenAnswer(invocation -> Optional.of(existingApplication));

        // Act & Assert
        assertThrows(AlreadyAppliedException.class, () -> jobApplicationServiceImpl.applyToCompany(user, company1.getId()));
    }

    @Test
    void testFindByEmployee() {
        // Arrange
        JobApplication jobApplication2 = new JobApplication();

        jobApplication.setUser(user);
        jobApplication2.setUser(user);

        List<JobApplication> expectedApplications = Arrays.asList(jobApplication, jobApplication2);

        // Mock jobApplicationRepository findByEmployee
        when(jobApplicationRepository.findByEmployee(user)).thenReturn(expectedApplications);

        // Act
        List<JobApplication> actualApplications = jobApplicationServiceImpl.findByEmployee(user);

        // Assert
        assertEquals(expectedApplications.size(), actualApplications.size());
        assertTrue(expectedApplications.containsAll(actualApplications));
    }




}