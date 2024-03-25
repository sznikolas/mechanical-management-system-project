package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.MachinePart;
import com.nikolas.mechanicalmanagementsystem.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface MachineService {
    BigDecimal calculateTotalPartsAmountByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies);
    BigDecimal calculateTotalChargedAmountByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies);
    BigDecimal calculateTotalProfitByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies);
    void calculateAndSetProfit(Machine machine);
    void saveMachine(Machine machine);
    Machine getMachineById(Long id);
    Machine getMachineForUser(User user, Long machineId);
    void updateMachine(Machine machine);
    void updateMachinePartsTax(Machine machine, BigDecimal newTaxInPercent);
    void deleteMachineById(Long id);
    void deleteInactiveMachineById(Long id);
    void deleteMachinePart(Long machineId, Long machinePartId);
    BigDecimal calculateAndSetPartsSum(Machine machine);
    MachinePart getMachinePartById(Long partId);
    void convertImagesToBase64(Machine machine);
    List<Machine> getActiveMachinesByYearMonthAndCompanyId(int year, int month, List<Long> companyIds);
    List<Machine> getDeletedMachines();

}
