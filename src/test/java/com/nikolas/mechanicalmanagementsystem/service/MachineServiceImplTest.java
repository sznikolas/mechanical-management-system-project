package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.exception.MachineNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.MachinePartRepository;
import com.nikolas.mechanicalmanagementsystem.repository.MachineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MachineServiceImplTest {
    @InjectMocks
    private MachineServiceImpl machineServiceImpl;
    @Mock
    private MachineRepository machineRepository;
    @Mock
    private MachinePartRepository machinePartRepository;
    private static Machine testMachine;
    private static User loggedInUser;

    @BeforeEach
    void setUp() {
        Company testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setCompanyName("Test Company");

        testMachine = new Machine();
        testMachine.setId(1L);
        testMachine.setMachineBrand("Test");
        testMachine.setMachineModel("Machine");
        testMachine.setDescription("Example description");
        testMachine.setActive(true);
        testMachine.setCompany(testCompany);

        Role roleCompanyRole = new Role();
        roleCompanyRole.setId(1L);
        roleCompanyRole.setRoleName("1_TEST_COMPANY_HUNGARY");

        CompanyRole companyRole = new CompanyRole();
        companyRole.setId(1L);
        companyRole.setRole(roleCompanyRole);
        companyRole.setCompany(testCompany);

        Set<Role> roles = new HashSet<>();
        roles.add(roleCompanyRole);

        Set<CompanyRole> companyRoles = new HashSet<>();
        companyRoles.add(companyRole);
        roleCompanyRole.setCompanyRoles(companyRoles);


        testCompany.setMachines(Set.of(testMachine));
        testCompany.setCompanyRoles(Set.of(companyRole));

        loggedInUser = new User();
        loggedInUser.setId(1L);
        loggedInUser.setFirstName("John");
        loggedInUser.setLastName("Doe");
        loggedInUser.setEmail("loggedinuser@gmail.com");
        loggedInUser.setRoles(roles);
        loggedInUser.setWorkplaces(List.of(testCompany));

    }

    @Test
    void testSaveMachine() {
        // Act
        machineServiceImpl.saveMachine(testMachine);

        // Assert
        verify(machineRepository, times(1)).save(testMachine);
    }

    @Test
    void testGetMachineById() {
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));

        // Act
        Machine actualMachine = machineServiceImpl.getMachineById(testMachine.getId());

        // Assert
        assertEquals(testMachine, actualMachine);
        verify(machineRepository, times(1)).findById(testMachine.getId());
    }

    @Test
    void testGetMachineForUser() {
        // Arrange
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));

        // Act
        Machine actualMachine = machineServiceImpl.getMachineForUser(loggedInUser, testMachine.getId());

        // Assert
        assertEquals(testMachine, actualMachine); // Verify that the returned machine is the expected one
        verify(machineRepository, times(1)).findById(testMachine.getId());
    }

    @Test
    void testGetMachineForUser_AccessDenied() {
        // Arrange
        testMachine.setActive(false);
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));

        // Assert
        assertThrows(AccessDeniedException.class, () -> machineServiceImpl.getMachineForUser(loggedInUser, testMachine.getId()));
        verify(machineRepository, times(1)).findById(testMachine.getId());
    }

    @Test
    void testCheckAccessRights_WithAccess() {
        // Act and Assert
        assertDoesNotThrow(() -> machineServiceImpl.checkAccessRights(loggedInUser, testMachine));
    }

    @Test
    void testCheckAccessRights_WithNoAccess() {
        Machine newMachine = new Machine();
        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> machineServiceImpl.checkAccessRights(loggedInUser, newMachine));
    }

    @Test
    void testUpdateMachine_SuccessfulUpdate() {
        // Arrange

        Machine updatedMachine = new Machine();
        updatedMachine.setId(1L);
        updatedMachine.setMachineBrand("NewBrand");
        updatedMachine.setMachineModel("NewModel");
        updatedMachine.setDescription("NewDescription");
        updatedMachine.setChargedAmount(BigDecimal.valueOf(200.0));

        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));
        when(machineRepository.save(any(Machine.class))).thenReturn(updatedMachine);

        // Act
        machineServiceImpl.updateMachine(updatedMachine);

        // Assert
        verify(machineRepository, times(1)).findById(1L);
        verify(machineRepository, times(2)).save(any(Machine.class));

        assertEquals("NewBrand", testMachine.getMachineBrand());
        assertEquals("NewModel", testMachine.getMachineModel());
        assertEquals("NewDescription", testMachine.getDescription());
        assertEquals(BigDecimal.valueOf(200.0), testMachine.getChargedAmount());
    }

    @Test
    void testDeleteMachineById_SuccessfulDeletion() {


        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));

        // Act
        machineServiceImpl.deleteMachineById(testMachine.getId());

        // Assert
        verify(machineRepository, times(1)).findById(testMachine.getId());
        verify(machineRepository, times(1)).deactivateMachine(testMachine.getId());
    }

    @Test
    void testDeleteMachineById_MachineNotFound() {

        // Stubbing repository method to return empty optional, simulating machine not found
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MachineNotFoundException.class, () -> machineServiceImpl.deleteMachineById(testMachine.getId()));

        // Verify that no interaction happened with the repository
        verify(machineRepository, never()).deactivateMachine(anyLong());
    }


    @Test
    void testDeleteInactiveMachineById_SuccessfulDeletion() {

        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));

        // Act
        machineServiceImpl.deleteInactiveMachineById(testMachine.getId());

        // Assert
        verify(machineRepository, times(1)).findById(testMachine.getId());
        verify(machineRepository, times(1)).deleteById(testMachine.getId());
    }

    @Test
    void testDeleteInactiveMachineById_MachineNotFound() {
        // Stubbing repository method to return empty optional, simulating machine not found
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MachineNotFoundException.class, () -> machineServiceImpl.deleteInactiveMachineById(testMachine.getId()));

        // Verify that no interaction happened with the repository
        verify(machineRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateMachinePartsTax_SuccessfulUpdate() {
        // Arrange
        BigDecimal initialTax = BigDecimal.TEN;
        testMachine.setTaxInPercent(initialTax);

        List<MachinePart> machineParts = new ArrayList<>();

        MachinePart machinePart1 = new MachinePart();
        machinePart1.setId(1L);
        machinePart1.setPartPrice(BigDecimal.valueOf(100));
        machinePart1.setTaxInPercent(initialTax);
        machineParts.add(machinePart1);

        MachinePart machinePart2 = new MachinePart();
        machinePart2.setId(2L);
        machinePart2.setPartPrice(BigDecimal.valueOf(200));
        machinePart2.setTaxInPercent(initialTax);
        machineParts.add(machinePart2);

        testMachine.setMachineParts(machineParts);

        BigDecimal newTax = BigDecimal.valueOf(15);

        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));
        when(machineRepository.save(testMachine)).thenReturn(testMachine);

        // Act
        machineServiceImpl.updateMachinePartsTax(testMachine, newTax);

        // Assert
        assertEquals(newTax, testMachine.getTaxInPercent());

        for (MachinePart part : testMachine.getMachineParts()) {
            BigDecimal expectedNewUnitTax = part.getPartPrice().multiply(newTax.divide(BigDecimal.valueOf(100)));
            assertEquals(newTax, part.getTaxInPercent());
            assertEquals(expectedNewUnitTax, part.getUnitTax());
            assertEquals(part.getPartPrice().add(expectedNewUnitTax), part.getPartPriceWithTax());
        }
        // Print old and new prices
        for (MachinePart part : testMachine.getMachineParts()) {
            BigDecimal oldPrice = part.getPartPrice();
            System.out.println("Old price: " + oldPrice);
            BigDecimal newPrice = oldPrice.add(part.getUnitTax());
            System.out.println("New price: " + newPrice);
        }
        verify(machineRepository, times(2)).save(testMachine);
    }

    @Test
    void testUpdateMachinePartsTax_InvalidTaxRate() {
        // Arrange
        BigDecimal invalidTaxRate = BigDecimal.valueOf(-10); // Invalid tax rate

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> machineServiceImpl.updateMachinePartsTax(testMachine, invalidTaxRate));
    }

    @Test
    void testCalculateAndSetPartsSum() {
        // Arrange
        Machine machine = new Machine();
        machine.setId(1L);

        BigDecimal totalPartPrice = BigDecimal.valueOf(300);
        when(machinePartRepository.calculateTotalPartPriceByMachineId(1L)).thenReturn(totalPartPrice);

        // Act
        BigDecimal result = machineServiceImpl.calculateAndSetPartsSum(machine);

        // Assert
        assertEquals(BigDecimal.valueOf(300), result);
        assertEquals(BigDecimal.valueOf(300), machine.getMachinePartsSum());
    }


    @Test
    void testCalculateTotalPartsAmountByMonthAndYearAndCompanies() {
        // Mocking the list of machines
        List<Machine> machines = new ArrayList<>();
        Machine machine1 = mock(Machine.class);
        Machine machine2 = mock(Machine.class);
        machines.add(machine1);
        machines.add(machine2);

        // Mocking the parts sum for each machine
        BigDecimal partsSum1 = BigDecimal.valueOf(100);
        BigDecimal partsSum2 = BigDecimal.valueOf(200);
        when(machine1.getMachinePartsSum()).thenReturn(partsSum1);
        when(machine2.getMachinePartsSum()).thenReturn(partsSum2);

        // Mocking the service method to return the list of machines
        when(machineServiceImpl.getActiveMachinesByMonthAndYear(1, 2024, List.of(1L, 2L))).thenReturn(machines);

        // Act
        BigDecimal result = machineServiceImpl.calculateTotalPartsAmountByMonthAndYearAndCompanies(1, 2024, List.of(1L, 2L));

        // Assert
        assertEquals(BigDecimal.valueOf(300), result);
    }

    @Test
    void testCalculateTotalChargedAmountByMonthAndYearAndCompanies() {

        // Mocking the list of machines
        List<Machine> machines = new ArrayList<>();
        Machine machine1 = mock(Machine.class);
        Machine machine2 = mock(Machine.class);
        machines.add(machine1);
        machines.add(machine2);

        // Mocking the charged amounts for each machine
        BigDecimal chargedAmount1 = BigDecimal.valueOf(100);
        BigDecimal chargedAmount2 = BigDecimal.valueOf(200);
        when(machine1.getChargedAmount()).thenReturn(chargedAmount1);
        when(machine2.getChargedAmount()).thenReturn(chargedAmount2);

        // Mocking the service method to return the list of machines
        when(machineServiceImpl.getActiveMachinesByMonthAndYear(1, 2024, List.of(1L, 2L))).thenReturn(machines);

        // Act
        BigDecimal result = machineServiceImpl.calculateTotalChargedAmountByMonthAndYearAndCompanies(1, 2024, List.of(1L, 2L));

        // Assert
        assertEquals(BigDecimal.valueOf(300), result);
    }

    @Test
    void testCalculateTotalProfitByMonthAndYearAndCompanies() {
        // Mocking the list of machines
        List<Machine> machines = new ArrayList<>();
        Machine machine1 = mock(Machine.class);
        Machine machine2 = mock(Machine.class);
        machines.add(machine1);
        machines.add(machine2);

        // Mocking the charged amounts and parts sum for each machine
        BigDecimal chargedAmount1 = BigDecimal.valueOf(100);
        BigDecimal partsSum1 = BigDecimal.valueOf(50);
        BigDecimal profit1 = chargedAmount1.subtract(partsSum1);

        BigDecimal chargedAmount2 = BigDecimal.valueOf(200);
        BigDecimal partsSum2 = BigDecimal.valueOf(150);
        BigDecimal profit2 = chargedAmount2.subtract(partsSum2);

        when(machine1.getChargedAmount()).thenReturn(chargedAmount1);
        when(machine1.getMachinePartsSum()).thenReturn(partsSum1);

        when(machine2.getChargedAmount()).thenReturn(chargedAmount2);
        when(machine2.getMachinePartsSum()).thenReturn(partsSum2);

        // Mocking the service method to return the list of machines
        when(machineServiceImpl.getActiveMachinesByMonthAndYear(1, 2024, List.of(1L, 2L))).thenReturn(machines);

        // Act
        BigDecimal result = machineServiceImpl.calculateTotalProfitByMonthAndYearAndCompanies(1, 2024, List.of(1L, 2L));

        // Assert
        assertEquals(profit1.add(profit2), result);
    }

    @Test
    void testGetActiveMachinesByMonthAndYear() {
        Integer selectedMonth = 1;
        Integer selectedYear = 2024;
        List<Long> selectedCompanies = List.of(1L, 2L);

        List<Machine> expectedMachines = new ArrayList<>();
        Machine machine1 = new Machine();
        Machine machine2 = new Machine();
        expectedMachines.add(machine1);
        expectedMachines.add(machine2);

        // Mocking the repository method to return the list of machines
        when(machineRepository.findByYearAndMonthAndCompanyIdInAndIsActive(selectedMonth, selectedYear, selectedCompanies, true)).thenReturn(expectedMachines);

        // Act
        List<Machine> result = machineServiceImpl.getActiveMachinesByMonthAndYear(selectedMonth, selectedYear, selectedCompanies);

        // Assert
        assertEquals(expectedMachines, result);
    }

    @Test
    void testDeleteMachinePart() {
        List<MachinePart> machineParts = new ArrayList<>();
        MachinePart machinePart = new MachinePart();
        machinePart.setId(2L);
        machinePart.setPartName("Breaks");
        machineParts.add(machinePart);
        testMachine.setMachineParts(machineParts);

        // Mocking repository methods
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));
        when(machinePartRepository.findById(machinePart.getId())).thenReturn(Optional.of(machinePart));
        when(machineRepository.save(testMachine)).thenReturn(testMachine);

        // Act
        machineServiceImpl.deleteMachinePart(testMachine.getId(), machinePart.getId());

        // Assert
        assertFalse(testMachine.getMachineParts().contains(machinePart));
        verify(machinePartRepository, times(1)).delete(machinePart);
        verify(machineRepository, times(2)).save(testMachine);
    }

    @Test
    void testDeleteMachinePart_ThrowsNotFoundException() {
        // Arrange
        MachinePart machinePart = new MachinePart();
        machinePart.setId(1L);

        // Mock the repository methods
        when(machineRepository.findById(testMachine.getId())).thenReturn(Optional.of(testMachine));
        when(machinePartRepository.findById(machinePart.getId())).thenReturn(Optional.of(machinePart));

        // Act and Assert
        assertThrows(NotFoundException.class, () -> machineServiceImpl.deleteMachinePart(testMachine.getId(), machinePart.getId()));
    }

    @Test
    void testConvertImagesToBase64() {
        // Arrange
        Images image1 = new Images();
        Images image2 = new Images();
        byte[] imageData1 = "Test image data 1".getBytes();
        byte[] imageData2 = "Test image data 2".getBytes();
        image1.setPicByte(imageData1);
        image2.setPicByte(imageData2);
        List<Images> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        Machine machine = new Machine();
        machine.setImages(images);

        // Act
        machineServiceImpl.convertImagesToBase64(machine);

        // Assert
        for (Images image : machine.getImages()) {
            String expectedBase64 = Base64.getEncoder().encodeToString(image.getPicByte());
            assertEquals(expectedBase64, image.getBase64Image());
        }
    }

    @Test
    void testConvertImagesToBase64_ImageDataNull() {
        // Arrange
        List<Images> images = new ArrayList<>();
        Images image = new Images();
        image.setPicByte(null); // Null imageData
        images.add(image);
        Machine machine = new Machine();
        machine.setImages(images);

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> machineServiceImpl.convertImagesToBase64(machine));

        // Assert
        String expectedMessage = "Image data is null or empty.";
        String actualMessage = exception.getMessage();
        assert actualMessage.contains(expectedMessage);
    }

    @Test
    void testConvertImagesToBase64_ImageDataEmpty() {
        // Arrange
        List<Images> images = new ArrayList<>();
        Machine machine = new Machine();
        machine.setImages(images);
        Images image = new Images();
        images.add(image);
        image.setPicByte(new byte[0]); // Empty imageData

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> machineServiceImpl.convertImagesToBase64(machine));

        // Assert
        String expectedMessage = "Image data is null or empty.";
        String actualMessage = exception.getMessage();
        assert actualMessage.contains(expectedMessage);
    }

    @Test
    void testGetActiveMachinesByYearMonthAndCompanyId() {
        // Arrange
        Machine machine = new Machine();
        machine.setActive(false);
        int year = 2024;
        int month = 3;
        List<Long> companyIds = Arrays.asList(1L, 2L, 3L);
        List<Machine> expectedActiveMachines = Collections.singletonList(testMachine);
        List<Machine> expectedInactiveMachines = List.of(machine);

        // Mocking repository behavior
        when(machineRepository.findByYearAndMonthAndCompanyIdInAndIsActive(year, month, companyIds, true))
                .thenReturn(expectedActiveMachines);

        // Act
        List<Machine> resultActive = machineServiceImpl.getActiveMachinesByYearMonthAndCompanyId(year, month, companyIds);

        // Assert
        assertEquals(expectedActiveMachines, resultActive);
        assertNotEquals(expectedActiveMachines, expectedInactiveMachines);
        verify(machineRepository, times(1)).findByYearAndMonthAndCompanyIdInAndIsActive(year, month, companyIds, true);
        verify(machineRepository, times(0)).findByYearAndMonthAndCompanyIdInAndIsActive(year, month, companyIds, false);
    }

    @Test
    void testGetDeletedMachines() {
        Machine inactiveMachine1 = new Machine();
        Machine inactiveMachine2 = new Machine();
        List<Machine> inactiveMachines = new ArrayList<>();
        inactiveMachines.add(inactiveMachine1);
        inactiveMachines.add(inactiveMachine2);

        // Mock
        when(machineRepository.findAllByRegistrationDateDeleted()).thenReturn(inactiveMachines);

        // Method call
        List<Machine> result = machineServiceImpl.getDeletedMachines();

        // Assert
        assertEquals(inactiveMachines, result);
    }

}