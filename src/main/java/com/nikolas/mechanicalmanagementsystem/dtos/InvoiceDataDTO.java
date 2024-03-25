package com.nikolas.mechanicalmanagementsystem.dtos;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class InvoiceDataDTO {
    private Company company;
    private Machine machine;

    private String employeeFirstName;
    private String employeeLastName;

    private String currentDate;
    private String futureDate;
}
