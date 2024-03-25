package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.InvoiceDataDTO;
import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {
    @InjectMocks
    private InvoiceServiceImpl invoiceServiceImpl;
    @Mock
    private UserService userService;
    @Mock
    private MachineService machineService;

    @Test
    void testGenerateInvoice() {
        // Arrange
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        LocalDateTime currentDateTime = LocalDateTime.now();

        String formattedCurrentDate = currentDateTime.format(formatter);
        String formattedFutureDate = currentDateTime.plusDays(7).format(formatter);

        User loggedInUser = new User();
        loggedInUser.setFirstName("John");
        loggedInUser.setLastName("Doe");

        Company company = new Company();
        company.setCompanyName("Test Company");

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setCompany(company);

        // Mock userService getLoggedInUser
        when(userService.getLoggedInUser()).thenReturn(loggedInUser);

        // Mock machineService getMachineForUser
        when(machineService.getMachineForUser(loggedInUser, machine.getId())).thenReturn(machine);

        // Act
        InvoiceDataDTO invoiceDataDTO = invoiceServiceImpl.generateInvoice(machine.getId());

        // Assert
        assertNotNull(invoiceDataDTO);
        assertEquals(loggedInUser.getFirstName(), invoiceDataDTO.getEmployeeFirstName());
        assertEquals(loggedInUser.getLastName(), invoiceDataDTO.getEmployeeLastName());
        assertEquals(machine, invoiceDataDTO.getMachine());
        assertEquals(machine.getCompany(), invoiceDataDTO.getCompany());
        assertEquals(formattedCurrentDate, invoiceDataDTO.getCurrentDate());
        assertEquals(formattedFutureDate, invoiceDataDTO.getFutureDate());
    }


}