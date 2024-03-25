package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.InvoiceDataDTO;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService{
    private final MachineService machineService;
    private final UserService userService;

    /**
     * Generates invoice data for a machine owned by the logged-in user.
     *
     * @param machineId The ID of the machine for which to generate the invoice data.
     * @return An {@code InvoiceDataDTO} object containing the generated invoice data.
     */
    @Override
    public InvoiceDataDTO generateInvoice(Long machineId) {
        // Get the logged-in user
        User loggedInUser = userService.getLoggedInUser();
        // Get the machine access by the logged-in user
        Machine machine = machineService.getMachineForUser(loggedInUser, machineId);

        // Generate current and future dates
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime futureDate = currentDate.plusDays(7);

        // Format dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        String formattedCurrentDate = currentDate.format(formatter);
        String formattedFutureDate = futureDate.format(formatter);

        // Compile data in the InvoiceDataDTO object
        InvoiceDataDTO invoiceDataDTO = new InvoiceDataDTO();
        invoiceDataDTO.setCompany(machine.getCompany());
        invoiceDataDTO.setEmployeeFirstName(loggedInUser.getFirstName());
        invoiceDataDTO.setEmployeeLastName(loggedInUser.getLastName());
        invoiceDataDTO.setMachine(machine);
        invoiceDataDTO.setCurrentDate(formattedCurrentDate);
        invoiceDataDTO.setFutureDate(formattedFutureDate);
        log.info("InvoiceServiceImpl::generateInvoice - Invoice generated");
        return invoiceDataDTO;
    }
}