package com.nikolas.mechanicalmanagementsystem.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class CompanyDTO {
    private String companyName;
    private String description;
    private String country;
    private String location;
    private String street;
    private int postCode;
    private LocalDateTime registrationDate;
    private String owner;

}


