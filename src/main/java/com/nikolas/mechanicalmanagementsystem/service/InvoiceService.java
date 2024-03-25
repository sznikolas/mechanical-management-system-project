package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.dtos.InvoiceDataDTO;

public interface InvoiceService {
    InvoiceDataDTO generateInvoice(Long machineId);
}
