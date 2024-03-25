package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.CompanyDTO;
import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.exception.CompanyNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {
    @InjectMocks
    private CompanyServiceImpl companyServiceImpl;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CompanyRoleRepository companyRoleRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private MachineService machineService;
    @Mock
    private HttpSession session;




    private static Company company;
//    private static Company company2;
    private static User loggedInUser;
//    private static CompanyRole companyRole;
//    private static Set<Role> role;


    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setCompanyName("Test Company");
        company.setCountry("Hungary");

        CompanyRole companyRole = new CompanyRole();
        companyRole.setCompany(company);
        companyRole.setRole(new Role("1_TEST COMPANY_HUNGARY"));
//        role.setCompanyRoles(Set.of(companyRole));

        Set<Role> role = new HashSet<>();
        role.add(companyRole.getRole());

        company.setCompanyRoles(Set.of(companyRole));

        loggedInUser = new User();
        loggedInUser.setId(1L);
        loggedInUser.setFirstName("John");
        loggedInUser.setLastName("Doe");
        loggedInUser.setEmail("loggedinuser@gmail.com");
        loggedInUser.setRoles(role);

    }

    @Test
    public void testFindById_ExistingCompany() {
        // Arrange

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        // Act
        Company actualCompany = companyServiceImpl.findById(company.getId());

        // Assert
        assertEquals(company, actualCompany);
    }

    @Test
    public void testFindById_NonExistingCompany() {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(CompanyNotFoundException.class, () -> companyServiceImpl.findById(company.getId()));
    }

    @Test
    public void testGetAllCompanies() {
        // Arrange
        List<Company> expectedCompanies = new ArrayList<>();
        Company company2 = new Company();
        expectedCompanies.add(company);
        expectedCompanies.add(company2);

        when(companyRepository.findAll()).thenReturn(expectedCompanies);

        // Act
        List<Company> actualCompanies = companyServiceImpl.getAllCompanies();

        // Assert
        assertEquals(expectedCompanies.size(), actualCompanies.size());
        assertEquals(expectedCompanies, actualCompanies);
    }

    @Test
    public void testCheckAccessRightsToOneCompany_AccessDenied() {
        // Arrange
        User user = new User();

        Company companyWithNoAccess = new Company();
        Role testRole = new Role("TEST_ROLE");

        CompanyRole testCompanyRole = new CompanyRole();
        testCompanyRole.setCompany(companyWithNoAccess);
        testCompanyRole.setRole(testRole);

        companyWithNoAccess.setCompanyRoles(Set.of(testCompanyRole));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> companyServiceImpl.checkAccessRightsToOneCompany(user, companyWithNoAccess));
    }

    @Test
    public void testCheckAccessRightsToOneCompany_AccessGranted() {
        // Arrange
        User user = new User();
        Company company = new Company();
        company.setId(6L);
        company.setCompanyName("Test Company");

        CompanyRole companyRole = new CompanyRole();
        companyRole.setCompany(company);
        companyRole.setId(5L);

        Set<CompanyRole> companyRoles = new HashSet<>();
        companyRoles.add(companyRole);

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setCompanyRoles(companyRoles);

        roles.add(role);
        user.setRoles(roles);

        // Act & Assert
        assertDoesNotThrow(() -> companyServiceImpl.checkAccessRightsToOneCompany(user, company));
        // If no exception is thrown, access is granted
    }

    @Test
    public void testIsLeaderOrDeputyLeader_Leader() {
        // Arrange
        company.setOwner(loggedInUser);
        // Act
        boolean result = companyServiceImpl.isLeaderOrDeputyLeader(loggedInUser, company);

        // Assert
        assertTrue(result);
    }
    @Test
    public void testIsLeaderOrDeputyLeader_DeputyLeader() {
        // Arrange
        company.setOwner(new User());
        company.setDeputyLeader(loggedInUser);
        // Act
        boolean result = companyServiceImpl.isLeaderOrDeputyLeader(loggedInUser, company);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsLeaderOrDeputyLeader_NotLeaderOrDeputyLeader() {
        // Arrange
        company.setOwner(loggedInUser);
        User otherUser = new User();
        // Act
        boolean result = companyServiceImpl.isLeaderOrDeputyLeader(otherUser, company);
        // Assert
        assertFalse(result);
    }

    @Test
    public void testUpdateCompany() {
        // Arrange
        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setCompanyName("Updated Company");
        updatedCompany.setCountry("Austria");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(updatedCompany);

        // Act
        System.out.println("Before update: " + company.getCompanyName());
        companyServiceImpl.updateCompany(updatedCompany);
        System.out.println("After update: " + company.getCompanyName());

        // Assert
        assertEquals("Updated Company", company.getCompanyName());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    public void testUpdateCompany_NewOwnerNotNull() {
        // Arrange
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        User newUser = new User();
        newUser.setLastName("User");
        newUser.setFirstName("Test");

        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setOwner(newUser); // Setting a non-null owner
        company.setOwner(loggedInUser);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        System.out.println("Company owner before update: " + company.getOwner().getFirstName());
        companyServiceImpl.updateCompany(updatedCompany);
        System.out.println("Company owner after update: " + company.getOwner().getFirstName());

        // Assert
        verify(companyRepository, times(1)).save(companyCaptor.capture());
        Company capturedCompany = companyCaptor.getValue();
        assertEquals("Test", capturedCompany.getOwner().getFirstName()); // Asserting that the owner is updated
    }

    @Test
    public void testUpdateCompany_NewOwnerNull() {
        // Arrange
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setOwner(null); // Setting a null owner
        company.setOwner(loggedInUser);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        System.out.println("Company owner before update: " + company.getOwner().getFirstName());
        companyServiceImpl.updateCompany(updatedCompany);
        System.out.println("Company owner after update: " + company.getOwner().getFirstName());

        // Assert
        verify(companyRepository, times(1)).save(companyCaptor.capture());
        Company capturedCompany = companyCaptor.getValue();
        assertEquals("John", capturedCompany.getOwner().getFirstName()); // Asserting that the owner is not updated
    }

    @Test
    public void testUpdateCompanyDeputyLeader_NewDeputyLeader() {
        // Arrange
        User newDeputyLeader = new User();
        newDeputyLeader.setId(2L);
        newDeputyLeader.setFirstName("New");
        newDeputyLeader.setLastName("Deputy Leader");

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(userRepository.findById(newDeputyLeader.getId())).thenReturn(Optional.of(newDeputyLeader));

        // Act
        companyServiceImpl.updateCompanyDeputyLeader(company.getId(), newDeputyLeader.getId());

        // Assert
        assertEquals(newDeputyLeader, company.getDeputyLeader());
    }

    @Test
    public void testUpdateCompanyDeputyLeader_RemoveDeputyLeader() {
        // Arrange
        User existingDeputyLeader = new User();
        existingDeputyLeader.setId(2L);
        existingDeputyLeader.setFirstName("Existing");
        existingDeputyLeader.setLastName("Deputy Leader");

        company.setDeputyLeader(existingDeputyLeader);

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        // Act
        companyServiceImpl.updateCompanyDeputyLeader(company.getId(), null);

        // Assert
        assertNull(company.getDeputyLeader());
    }

    @Test
    public void testUpdateCompanyDeputyLeader_ExceptionHandling() {
        // Arrange
        Long deputyLeaderId = 2L;

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(userRepository.findById(deputyLeaderId)).thenThrow(new NotFoundException("Deputy leader not found with id: " + deputyLeaderId));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> companyServiceImpl.updateCompanyDeputyLeader(company.getId(), deputyLeaderId));

    }

    @Test
    public void testCreateCompany_Success() {
        // Arrange
        Role role = new Role("1_TEST_COMPANY_HUNGARY");

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(loggedInUser));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        doNothing().when(userService).saveUser(any(User.class));
        doReturn(null).when(companyRoleRepository).save(any(CompanyRole.class));

        // Act
        companyServiceImpl.createCompany(company, loggedInUser);

        // Assert
        verify(userService, times(1)).findByEmail(anyString());
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(companyRepository, times(1)).save(any(Company.class));
        verify(userService, times(1)).saveUser(any(User.class));
        verify(companyRoleRepository, times(1)).save(any(CompanyRole.class));
    }

    @Test
    public void testCreateCompany_WhenUserNotFound() {

        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NotFoundException.class, () -> companyServiceImpl.createCompany(company, loggedInUser));
        // Verify that no save method was called
        verify(companyRepository, never()).save(any(Company.class));
        verify(roleRepository, never()).save(any(Role.class));
        verify(companyRoleRepository, never()).save(any(CompanyRole.class));
    }

    @Test
    public void testCreateCompany_UnexpectedError() {
        // Arrange
        Company company = new Company();

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(loggedInUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> companyServiceImpl.createCompany(company, loggedInUser));
        assertEquals("Unexpected error occurred while creating company", exception.getMessage());

    }

    @Test
    public void testDeleteCompanyById_ExceptionHandling() {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> companyServiceImpl.deleteCompanyById(company.getId()));
        assertEquals("Company doesn't exist with id: " + company.getId(), exception.getMessage());

        // Verify no other deletions happened
        verify(roleRepository, never()).deleteAll(any());
        verify(companyRoleRepository, never()).deleteAll(any());
        verify(jobApplicationRepository, never()).deleteAll(any());
        verify(userService, never()).removeRolesFromUsers(any());
    }

    @Test
    public void testDeleteCompanyById_RuntimeException() {
        // Arrange
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(new Company()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> companyServiceImpl.deleteCompanyById(company.getId()));
        assertEquals("Unexpected error occurred while creating company", exception.getMessage());

        // Verify no other deletions happened
        verify(roleRepository, never()).deleteAll(any());
        verify(companyRoleRepository, never()).deleteAll(any());
        verify(jobApplicationRepository, never()).deleteAll(any());
        verify(userService, never()).removeRolesFromUsers(any());
    }

    @Test
    public void testDeleteCompanyById_Success() {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        // Act
        companyServiceImpl.deleteCompanyById(company.getId());

        // Assert
        verify(jobApplicationRepository).deleteAll(company.getJobApplications());
        verify(userService).removeRolesFromUsers(any());
        verify(companyRoleRepository).deleteAll(company.getCompanyRoles());
        verify(roleRepository).deleteAll(any());
        verify(companyRepository).delete(company);
    }

    @Test
    public void testGetAllCompaniesByUser_AdminUser() {
        // Arrange
        Role adminRole = new Role("ADMIN");
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(adminRole));

        Company company1 = new Company();
        company1.setCompanyName("Test Company1");

        List<Company> expectedCompanies = Arrays.asList(company,company1);
        when(companyRepository.findAll()).thenReturn(expectedCompanies);
        // Act
        List<Company> actualCompanies = companyServiceImpl.getAllCompaniesByUser(adminUser);
        // Assert
        assertEquals(expectedCompanies.size(), actualCompanies.size());
        assertEquals(expectedCompanies, actualCompanies);
        verify(userService, never()).getWorkplacesForLoggedInUser(anyString());
        verify(companyRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllCompaniesByUser_NonAdminUser() {
        // Arrange
        Role userRole = new Role("USER");
        User nonAdminUser = new User();
        nonAdminUser.setEmail("user@example.com");
        nonAdminUser.setRoles(Set.of(userRole));

        Company company1 = new Company();
        company1.setCompanyName("Test Company1");
        List<Company> expectedCompanies = Arrays.asList(company,company1);

        when(userService.getWorkplacesForLoggedInUser(nonAdminUser.getEmail())).thenReturn(expectedCompanies);
        // Act
        List<Company> actualCompanies = companyServiceImpl.getAllCompaniesByUser(nonAdminUser);
        // Assert
        assertEquals(expectedCompanies.size(), actualCompanies.size());
        assertEquals(expectedCompanies, actualCompanies);
        assertNotEquals(3,actualCompanies.size());
        verify(companyRepository, never()).findAll();
        verify(userService, times(1)).getWorkplacesForLoggedInUser(anyString());
    }

    @Test
    public void testGetAllCompaniesByUser_WorkplacesRetrievalThrowsException() {
        // Arrange
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("example@example.com");

        Set<Role> userRoles = new HashSet<>();
        when(user.getRoles()).thenReturn(userRoles);

        when(userService.getWorkplacesForLoggedInUser(anyString())).thenThrow(new RuntimeException("Error retrieving workplaces"));

        // Act
        List<Company> result = companyServiceImpl.getAllCompaniesByUser(user);

        // Assert
        assertTrue(result.isEmpty());
        verify(userService, times(1)).getWorkplacesForLoggedInUser(anyString());
        verify(companyRepository, never()).findAll();
    }

    @Test
    public void testGetJobApplicationsByCompanyId() {
        // Arrange
        Long companyId = 1L;

        JobApplication application1 = new JobApplication();
        application1.setAccepted(false);
        JobApplication application2 = new JobApplication();
        application2.setAccepted(true);

        Set<JobApplication> jobApplications = new HashSet<>();
        jobApplications.add(application1);
        jobApplications.add(application2);

        Company company = new Company();
        company.setId(companyId);
        company.setJobApplications(jobApplications);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // Act
        Set<JobApplication> result = companyServiceImpl.getJobApplicationsByCompanyId(companyId);

        // Assert
        assertEquals(1, result.size()); // "Only the pending job applications should be returned
        assertTrue(result.contains(application1)); // Application 1 is pending
        assertFalse(result.contains(application2)); // Application 2 is accepted

        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    public void testGetJobApplicationsByCompanyId_CompanyNotFound() {
        // Arrange
        Long companyId = 1L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        // Assert
        assertThrows(CompanyNotFoundException.class, () -> companyServiceImpl.getJobApplicationsByCompanyId(companyId));
        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    public void testGetEmployeesByCompanyId() {
        // Arrange
        User testUser = new User();
        testUser.setFirstName("ALice");

        Set<User> employees = new HashSet<>();
        employees.add(loggedInUser);
        employees.add(testUser);

        company.setEmployees(employees);

        when(companyRepository.findById(anyLong())).thenReturn(java.util.Optional.of(company));

        // Act
        Set<User> result = companyServiceImpl.getEmployeesByCompanyId(company.getId());

        // Assert
        Assertions.assertEquals(employees.size(), result.size());
        // Asserting that each employee in the result set is contained in the original set
        for (User employee : result) {
            Assertions.assertTrue(employees.contains(employee));
        }

        verify(companyRepository, times(1)).findById(company.getId());
    }


    @Test
    void testGetAllEmployeesExceptCurrentUserAndOwner() {
        // Mocking data
        User testUser = new User();
        testUser.setFirstName("ALice");
        testUser.setId(2L);

        User employee = new User();
        employee.setFirstName("Employee1");
        employee.setId(3L);


        Set<User> employees = new HashSet<>();
        employees.add(loggedInUser);
        employees.add(testUser);
        employees.add(employee);

        company.setOwner(loggedInUser);
        company.setEmployees(employees);

        // Stubbing repository method
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        // Calling the method under test
        Set<User> result = companyServiceImpl.getAllEmployeesExceptCurrentUserAndOwner(testUser, company.getId());

        // Verification
        assertEquals(1, result.size()); // We expect only one employee to remain
        assertEquals("Employee1", result.iterator().next().getFirstName()); // Make sure the correct employee remains

        // Verify interactions with mock objects
        verify(companyRepository, times(1)).findById(company.getId());
    }

    @Test
    @Transactional
    void testAcceptJobApplication() {
        // Mocking data
        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(1L);
        jobApplication.setAccepted(false); // Initial state
        jobApplication.setCompany(company);
        jobApplication.setEmployee(loggedInUser);

        Role role1 = new Role();
        role1.setId(1L);
        role1.setRoleName("Role1");

        List<Role> roles = List.of(role1);

        CompanyRole companyRole = new CompanyRole();
        companyRole.setId(1L);
        companyRole.setRole(role1);
        companyRole.setCompany(company);

        Set<CompanyRole> companyRoles = new HashSet<>();
        companyRoles.add(companyRole);

        company.setCompanyRoles(companyRoles);


        // Stubbing repository methods
        when(jobApplicationRepository.findById(jobApplication.getId())).thenReturn(Optional.of(jobApplication));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(roleRepository.findAllById(any())).thenReturn(roles);

        // Calling the method under test
        System.out.println("User roles before joined the company " + loggedInUser.getRoles().stream().map(Role::getRoleName).toList());
        companyServiceImpl.acceptJobApplication(jobApplication.getId());
        System.out.println("User roles after joined the company: " + loggedInUser.getRoles().stream().map(Role::getRoleName).toList());

        // Verification
        assertTrue(jobApplication.isAccepted()); // Make sure the job application is accepted
        assertTrue(company.getEmployees().contains(loggedInUser)); // Make sure the applicant is added to the company's employees
        assertTrue(loggedInUser.getRoles().containsAll(roles)); // Make sure the applicant has all the necessary roles

        // Verify interactions with mock objects
        verify(jobApplicationRepository, times(1)).findById(jobApplication.getId());
        verify(companyRepository, times(1)).save(any(Company.class));
        verify(roleRepository, times(1)).findAllById(any());
    }

    @Test
    @Transactional
    void testRejectJobApplication() {
        // Mocking data
        Long jobApplicationId = 1L;
        JobApplication jobApplication = new JobApplication();
        jobApplication.setId(jobApplicationId);
        jobApplication.setUser(loggedInUser);
        jobApplication.setCompany(company);

        // Stubbing repository method
        when(jobApplicationRepository.findById(jobApplicationId)).thenReturn(Optional.of(jobApplication));

        // Calling the method under test
        companyServiceImpl.rejectJobApplication(jobApplicationId);

        // Verification
        verify(jobApplicationRepository, times(1)).findById(jobApplicationId);
        verify(jobApplicationRepository, times(1)).delete(jobApplication);
    }

    @Test
    @Transactional
    void testRemoveEmployeeFromCompany() {
        // Mocking data
        Set<User> employees = new HashSet<>();
        employees.add(loggedInUser);
        company.setEmployees(employees);

        // Stubbing repository methods
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(userService.findById(loggedInUser.getId())).thenReturn(Optional.of(loggedInUser));
        when(roleRepository.findAllById(any())).thenReturn(new ArrayList<>());

        // Calling the method under test
        companyServiceImpl.removeEmployeeFromCompany(company.getId(), loggedInUser.getId());

        // Verification
        assertFalse(company.getEmployees().contains(loggedInUser)); // Make sure the employee is removed from the company's employees

        // Verify interactions with mock objects
        verify(companyRepository, times(2)).findById(company.getId());
        verify(userService, times(1)).findById(loggedInUser.getId());
        verify(roleRepository, times(1)).findAllById(any());
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    void testLeaveCompany() {
        // Mocking data
        User user = new User();
        user.setId(5L);
        user.setFirstName("Test");
        user.setLastName("User");

        Set<User> employees = new HashSet<>();
        employees.add(user);
        company.setEmployees(employees);

        Role role1 = new Role();
        role1.setId(1L);
        role1.setRoleName("Role1");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setRoleName("Role2");

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        user.setRoles(roles);

        CompanyRole companyRole = new CompanyRole();
        companyRole.setId(1L);
        companyRole.setRole(role1);
        companyRole.setCompany(company);

        Set<CompanyRole> companyRoles = new HashSet<>();
        companyRoles.add(companyRole);

        company.setCompanyRoles(companyRoles);
        company.setDeputyLeader(user);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Stubbing repository methods
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(roleRepository.findAllById(any())).thenReturn(new ArrayList<>(roles));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(loggedInUser));

        // Calling the method under test
        companyServiceImpl.leaveCompany(user.getId(), company.getId(), request, response);

        // Verification
        verify(userRepository, times(1)).findById(user.getId());
        verify(companyRepository, times(1)).findById(company.getId());
        verify(roleRepository, times(1)).findAllById(any());
        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(companyRepository, times(1)).save(company);
        verify(userRepository, times(1)).save(user);

        // Assert that user is removed from company
        assertFalse(company.getEmployees().contains(user));
        // Assert that roles are removed from user
        assertTrue(user.getRoles().isEmpty());
        // Assert that new owner and deputy leader are set correctly
        assertEquals(loggedInUser, company.getOwner());
        assertEquals(loggedInUser, company.getDeputyLeader());
    }

    @Test
    void testAddNewMachineToCompany() {
        // Mocking data
        Machine machine = new Machine();
        machine.setId(1L);
        machine.setMachineBrand("Brand");
        machine.setMachineModel("Model");

        company.setOwner(loggedInUser); // Setting owner of the company

        // Stubbing repository methods
        when(userService.getLoggedInUser()).thenReturn(loggedInUser);
        when(userService.findByEmail(loggedInUser.getEmail())).thenReturn(Optional.of(loggedInUser));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        // Stubbing the calculation of profit
        doNothing().when(machineService).calculateAndSetProfit(machine);

        // Calling the method under test
        companyServiceImpl.addNewMachineToCompany(machine, company.getId());

        // Verification
        verify(userService, times(1)).getLoggedInUser();
        verify(userService, times(1)).findByEmail(loggedInUser.getEmail());
        verify(companyRepository, times(1)).findById(company.getId());
        verify(machineService, times(1)).calculateAndSetProfit(machine);
        verify(companyRepository, times(1)).save(company); // Ensure company is saved with new machine

        // Assert that machine is added to company
        assertTrue(company.getMachines().contains(machine));
    }

    @Test
    void testAddNewMachineToCompany_AccessDenied() {
        // Mocking data
        User testUser = new User();
        Machine machine = new Machine();
        machine.setId(1L);
        machine.setMachineBrand("Brand");
        machine.setMachineModel("Model");

        company.setOwner(testUser); // Setting owner of the company

        // Stubbing repository methods
        when(userService.getLoggedInUser()).thenReturn(loggedInUser);
        when(userService.findByEmail(loggedInUser.getEmail())).thenReturn(Optional.of(loggedInUser));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        assertThrows(AccessDeniedException.class, () -> companyServiceImpl.addNewMachineToCompany(machine, company.getId()));
    }

    @Test
    void testGetCompanyDetails() {
        // Mocking data
        Long companyId = 1L;
        String companyName = "Test Company";
        String description = "Test Description";
        String country = "Test Country";
        String location = "Test Location";
        String street = "Test Street";
        Integer postCode = 12345;
        LocalDateTime registrationDate = LocalDateTime.of(2022, 1, 1, 0 ,0 ,0);
        String ownerFirstName = "John";
        String ownerLastName = "Doe";

        Company company = new Company();
        company.setId(companyId);
        company.setCompanyName(companyName);
        company.setDescription(description);
        company.setCountry(country);
        company.setLocation(location);
        company.setStreet(street);
        company.setPostCode(postCode);
        company.setRegistrationDate(registrationDate);

        User owner = new User();
        owner.setFirstName(ownerFirstName);
        owner.setLastName(ownerLastName);
        company.setOwner(owner);

        // Stubbing repository method
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // Calling the method under test
        CompanyDTO companyDTO = companyServiceImpl.getCompanyDetails(companyId);

        // Verification
        assertEquals(companyName, companyDTO.getCompanyName());
        assertEquals(description, companyDTO.getDescription());
        assertEquals(country, companyDTO.getCountry());
        assertEquals(location, companyDTO.getLocation());
        assertEquals(street, companyDTO.getStreet());
        assertEquals(postCode, companyDTO.getPostCode());
        assertEquals(registrationDate, companyDTO.getRegistrationDate());
        assertEquals(ownerFirstName + " " + ownerLastName, companyDTO.getOwner());
    }

    @Test
    void testGetSelectedCompaniesByIds_WithNullSessionAttribute() {
        // Mocking data
        Company company1 = new Company();
        company1.setId(1L);
        Company company2 = new Company();
        company2.setId(2L);
        Company company3 = new Company();
        company3.setId(3L);
        List<Company> userCompanies = Arrays.asList(company1, company2, company3);

        // Stubbing session method to return null
        when(session.getAttribute("selectedCompanies")).thenReturn(null);

        // Calling the method under test
        List<Long> result = companyServiceImpl.getSelectedCompaniesByIds(session, null, userCompanies);
        System.out.println("Selected companies: null, so we get back all company: " + userCompanies.stream().map(Company::getId).toList());
        // Verification
        assertEquals(userCompanies.stream().map(Company::getId).toList(), result); // Initially, selectedCompanies should be userAllCompanies
        verify(session, times(1)).setAttribute(eq("selectedCompanies"), eq(userCompanies.stream().map(Company::getId).collect(Collectors.toList())));
    }

    @Test
    void testGetSelectedCompaniesByIds_WithNotNullSessionAttribute() {
        // Mocking data
        Company company1 = new Company();
        company1.setId(1L);
        Company company2 = new Company();
        company2.setId(2L);
        Company company3 = new Company();
        company3.setId(3L);
        List<Company> userCompanies = Arrays.asList(company1, company2, company3);

        List<Long> selectedCompanyIds = Arrays.asList(1L, 2L);

        // Stubbing session method to return null
        when(session.getAttribute("selectedCompanies")).thenReturn(selectedCompanyIds);

        // Calling the method under test
        List<Long> result = companyServiceImpl.getSelectedCompaniesByIds(session, selectedCompanyIds, userCompanies);
        System.out.println("Selected companies: " + result + " from: " + userCompanies.stream().map(Company::getId).toList());

        // Verification
        assertEquals(selectedCompanyIds, result); // Initially, selectedCompanies should be userAllCompanies
        verify(session, times(1)).setAttribute(eq("selectedCompanies"), any(List.class));
    }

    @Test
    void testGetSelectedCompaniesByIds_AccessDeniedWhenInvalidSelectedCompanyIdsProvided() {
        // Mocking data
        Company company1 = new Company();
        company1.setId(1L);
        Company company2 = new Company();
        company2.setId(2L);
        Company company3 = new Company();
        company3.setId(3L);
        List<Company> userCompanies = Arrays.asList(company1, company2, company3);

        // Invalid selected company IDs
        List<Long> selectedCompanyIds = Arrays.asList(1L, 4L); // The user doesn't have access to company with ID 4

        // Calling the method under test and verifying the exception
        assertThrows(AccessDeniedException.class, () -> companyServiceImpl.getSelectedCompaniesByIds(session, selectedCompanyIds, userCompanies));
    }

    @Test
    void testGetMachinesByMonth() {
        // Stubbing session method to return null initially
        when(session.getAttribute("selectedMonth")).thenReturn(null);

        // Save the current month value from the local date
        Integer currentMonth = LocalDate.now().getMonthValue();

        // Calling the method under test without providing month parameter
        Integer result = companyServiceImpl.getMachinesByMonth(session, null);

        // Verification
        assertNotNull(result); // Check if the result is not null
        assertEquals(currentMonth, result); // Check if the result is between 1 and 12 (inclusive)
        verify(session, times(1)).setAttribute(eq("selectedMonth"), anyInt()); // Verify if setAttribute method is called once

    }

    @Test
    void testGetMachinesByMonth_WithSpecificMonth() {
        // Stubbing session method to return null initially
        when(session.getAttribute("selectedMonth")).thenReturn(null);

        // Calling the method under test providing a specific month parameter
        Integer specificMonth = 5;
        Integer result = companyServiceImpl.getMachinesByMonth(session, specificMonth);

        // Verification
        assertEquals(specificMonth, result); // Check if the result matches the specific month parameter
        verify(session, times(1)).setAttribute(eq("selectedMonth"), eq(specificMonth));
    }

    @Test
    void testGetMachinesByYear() {
        // Stubbing session method to return null initially
        when(session.getAttribute("selectedYear")).thenReturn(null);

        // Save the current year value from the local date
        Integer currentYear = LocalDate.now().getYear();

        // Calling the method under test without providing year parameter
        Integer result = companyServiceImpl.getMachinesByYear(session, null);

        // Verification
        assertNotNull(result); // Check if the result is not null
        assertEquals(currentYear, result); // Check if the result matches the current year
        verify(session, times(1)).setAttribute(eq("selectedYear"), eq(currentYear)); // Verify if setAttribute method is called once with current year
    }

    @Test
    void testGetMachinesByYear_WithSpecificYear() {
        // Stubbing session method to return null initially
        when(session.getAttribute("selectedYear")).thenReturn(null);

        Integer specificYear = 2023;
        Integer result = companyServiceImpl.getMachinesByYear(session, specificYear);

        // Verification
        assertNotNull(result); // Check if the result is not null
        assertEquals(specificYear, result); // Check if the result matches the current year
        verify(session, times(1)).setAttribute(eq("selectedYear"), eq(specificYear));
    }





}