package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.exception.MachineNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.MachinePartRepository;
import com.nikolas.mechanicalmanagementsystem.repository.MachineRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;
    private final MachinePartRepository machinePartRepository;

    // Saving machine
    @Transactional
    @Override
    public void saveMachine(Machine machine) {
        machineRepository.save(machine);
    }

    @Override
    public Machine getMachineById(Long machineId) {
        return machineRepository.findById(machineId).orElseThrow(() -> new MachineNotFoundException("Machine not found with id: " + machineId));
    }


    /**
     * Retrieves the machine associated with the provided machine ID for the given user, performing necessary access checks.
     *
     * @param user      The user for whom the machine access is being checked.
     * @param machineId The ID of the machine to retrieve.
     * @return The machine associated with the provided machine ID.
     * @throws MachineNotFoundException If no machine is found with the provided machine ID.
     * @throws AccessDeniedException    If the user does not have access to the machine or if the machine is inactive and the user does not have admin role.
     */
    public Machine getMachineForUser(User user, Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found with id: " + machineId));

        // Check access rights for the user
        checkAccessRights(user, machine);

        // If the machine is not active and the user does not have an admin role, deny access
        if (!machine.isActive() && !userHasAdminRole(user)) {
            throw new AccessDeniedException("Access denied to machine with id: " + machine.getId());
        }

//        log.info("MachineServiceImpl::getMachineForUser - Opened machine: " + machine.getMachineModel() + ' ' + machine.getMachineBrand());
        return machine;
    }


    /**
     * Checks if the provided user has access rights to the given machine.
     *
     * @param loggedInUser The user whose access rights are being checked.
     * @param machine      The machine for which access rights are being checked.
     * @throws AccessDeniedException If the user does not have access rights to the machine.
     */
    protected void checkAccessRights(User loggedInUser, Machine machine) {
        // Retrieve the companies associated with the user's roles
        List<Company> userCompanies = loggedInUser.getRoles().stream()
                .flatMap(role -> role.getCompanyRoles().stream())
                .map(CompanyRole::getCompany)
                .toList();
        // Check if any of the user's companies match the machine's company
        if (!userCompanies.contains(machine.getCompany())) {
            log.info("MachineServiceImpl::checkAccessRights - User {} has no access to machine {}", loggedInUser.getEmail(), machine.getId());
            throw new AccessDeniedException("Access denied to machine with id: " + machine.getId());
        }
    }


    /**
     * Checks whether the specified user has the admin role.
     *
     * @param user The user whose roles are being checked.
     * @return {@code true} if the user has the admin role, {@code false} otherwise.
     */
    private boolean userHasAdminRole(User user) {
        log.info("MachineServiceImpl::userHasAdminRole - User roles: " + user.getRoles().stream()
                .map(Role::getRoleName).toList());

        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleName()));
    }


    /**
     * Updates the details of the specified machine.
     *
     * @param machine The machine object containing the updated details.
     * @throws MachineNotFoundException if the machine with the specified ID is not found.
     */
    @Transactional
    @Override
    public void updateMachine(Machine machine) {
        Machine existingMachine = machineRepository.findById(machine.getId())
                .orElseThrow(() -> new MachineNotFoundException("Machine not found with id: " + machine.getId()));
        // Update the machine details with the new values
        existingMachine.setMachineBrand(machine.getMachineBrand());
        existingMachine.setMachineModel(machine.getMachineModel());
        existingMachine.setDescription(machine.getDescription());
        existingMachine.setChargedAmount(machine.getChargedAmount());

        existingMachine = machineRepository.save(existingMachine);

        // Calculate and set the sum of machine parts
        existingMachine.setMachinePartsSum(calculateAndSetPartsSum(existingMachine));
        // Calculate and set the profit for the machine
        calculateAndSetProfit(existingMachine);

        log.info("MachineServiceImpl::updateMachine - {} machine updated successfully!", existingMachine.getMachineBrand() + ' ' + existingMachine.getMachineModel());
        machineRepository.save(existingMachine);
    }


    /**
     * Deletes the machine with the specified ID.
     *
     * @param id The ID of the machine to delete.
     * @throws MachineNotFoundException if the machine with the specified ID is not found.
     */
    @Override
    @Transactional
    public void deleteMachineById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found with id: " + id));
        // Get the machine model and brand for logging purposes
        String machineModel = machine.getMachineModel();
        String machineBrand = machine.getMachineBrand();
        // Log that the machine is being inactivated
        log.info("MachineServiceImpl::deleteMachineById - Machine {} is inactivated!", machineModel + ' ' + machineBrand);
        // Deactivate the machine in the database
        machineRepository.deactivateMachine(id);
    }


    /**
     * Deletes the inactive machine with the specified ID permanently.
     *
     * @param id The ID of the machine to delete.
     * @throws MachineNotFoundException if the machine with the specified ID is not found.
     */
    @Override
    public void deleteInactiveMachineById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found with id: " + id));
        // Delete the machine from the database
        machineRepository.deleteById(id);
        // Log that the machine has been permanently deleted
        log.info("MachineServiceImpl::deleteInactiveMachineById - Machine {} permanently deleted!", machine.getMachineModel() + ' ' + machine.getMachineBrand());
    }


    /**
     * Calculates the profit for the specified machine based on the charged amount and the sum of machine parts.
     * Sets the calculated profit in the machine object.
     *
     * @param machine The machine for which to calculate and set the profit.
     */
    @Transactional
    @Override
    public void calculateAndSetProfit(Machine machine) {
        // Calculate the profit by subtracting the sum of machine parts from the charged amount
        BigDecimal profit = machine.getChargedAmount().subtract(machine.getMachinePartsSum());
        // Set the calculated profit in the machine object
        machine.setProfit(profit);
    }


    /**
     * Updates the tax information for the machine parts of the specified machine.
     * Calculates and sets the new unit tax and price with tax for each machine part based on the new tax rate.
     * Updates the machine's tax rate and saves the changes to the database.
     *
     * @param machine         The machine whose machine parts' tax information is to be updated.
     * @param newTaxInPercent The new tax rate to be applied to the machine parts.
     * @throws IllegalArgumentException if the new tax rate is null or outside the valid range (0 to 100).
     */
    @Override
    public void updateMachinePartsTax(Machine machine, BigDecimal newTaxInPercent) {
        List<MachinePart> machineParts = machine.getMachineParts();

        // Validate the new tax rate
        if (newTaxInPercent == null || newTaxInPercent.compareTo(BigDecimal.ZERO) < 0 || newTaxInPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Invalid tax rate: " + newTaxInPercent);
        }
        // Iterate through each machine part
        for (MachinePart machinePart : machineParts) {
            BigDecimal partPrice = machinePart.getPartPrice();

            // Calculate the new unit tax based on the new tax rate
            BigDecimal newUnitTax = partPrice.multiply(newTaxInPercent.divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
            // Calculate the new part price with tax
            BigDecimal newPartPriceWithTax = partPrice.add(newUnitTax);

            // Update the tax-related properties of the machine part
            machinePart.setTaxInPercent(newTaxInPercent);
            machinePart.setUnitTax(newUnitTax);
            machinePart.setPartPriceWithTax(newPartPriceWithTax);
        }

        // Update the machine's tax rate
        machine.setTaxInPercent(newTaxInPercent);
        updateMachine(machine);
        log.info("MachineServiceImpl::updateMachinePartsTax - {} machine TAX updated successfully to {} percent!", machine.getMachineBrand() + ' ' + machine.getMachineModel(), newTaxInPercent);
    }


    /**
     * Calculates the total price of all parts associated with the specified machine
     * and sets the sum to the machine's 'machinePartsSum' property.
     *
     * @param machine The machine for which the parts sum is to be calculated and set.
     * @return The sum of the parts calculated.
     */
    @Override
    public BigDecimal calculateAndSetPartsSum(Machine machine) {
        Long machineId = machine.getId();
        // Calculate the total price of all parts associated with the machine
        BigDecimal totalPartPrice = machinePartRepository.calculateTotalPartPriceByMachineId(machineId);
        // Set the sum of the parts to the machine
        BigDecimal partsSum = totalPartPrice != null ? totalPartPrice : BigDecimal.valueOf(0.0);
        machine.setMachinePartsSum(partsSum);
        return partsSum;
    }

    /**
     * Calculates the total price of parts for active machines belonging to the selected companies in the specified month and year.
     *
     * @param selectedMonth     The selected month.
     * @param selectedYear      The selected year.
     * @param selectedCompanies The list of selected company IDs.
     * @return The total amount of parts for active machines belonging to the selected companies in the specified month and year.
     */
    @Override
    public BigDecimal calculateTotalPartsAmountByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies) {
        List<Machine> machines = getActiveMachinesByMonthAndYear(selectedMonth, selectedYear, selectedCompanies);
        BigDecimal totalPartsAmount = BigDecimal.ZERO;

        for (Machine machine : machines) {
            totalPartsAmount = totalPartsAmount.add(machine.getMachinePartsSum());
        }
        return totalPartsAmount;
    }


    /**
     * Calculates the total charged amount for active machines belonging to the selected companies in the specified month and year.
     *
     * @param selectedMonth     The selected month.
     * @param selectedYear      The selected year.
     * @param selectedCompanies The list of selected company IDs.
     * @return The total charged amount for active machines belonging to the selected companies in the specified month and year.
     */
    @Override
    public BigDecimal calculateTotalChargedAmountByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies) {
        List<Machine> machines = getActiveMachinesByMonthAndYear(selectedMonth, selectedYear, selectedCompanies);
        BigDecimal totalChargedAmount = BigDecimal.ZERO;

        for (Machine machine : machines) {
            BigDecimal chargedAmount = machine.getChargedAmount();
            totalChargedAmount = totalChargedAmount.add(chargedAmount);
        }
        return totalChargedAmount;
    }


    /**
     * Calculates the total profit for active machines belonging to the selected companies in the specified month and year.
     *
     * @param selectedMonth     The selected month.
     * @param selectedYear      The selected year.
     * @param selectedCompanies The list of selected company IDs.
     * @return The total profit for active machines belonging to the selected companies in the specified month and year.
     */
    @Override
    public BigDecimal calculateTotalProfitByMonthAndYearAndCompanies(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies) {
        List<Machine> machines = getActiveMachinesByMonthAndYear(selectedMonth, selectedYear, selectedCompanies);
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Machine machine : machines) {
            BigDecimal chargedAmount = machine.getChargedAmount();
            BigDecimal partsSum = machine.getMachinePartsSum();
            BigDecimal profit = chargedAmount.subtract(partsSum);
            totalProfit = totalProfit.add(profit);
        }
        return totalProfit;
    }


    /**
     * Retrieves the list of active machines belonging to the selected companies in the specified month and year.
     *
     * @param selectedMonth     The selected month.
     * @param selectedYear      The selected year.
     * @param selectedCompanies The list of selected company IDs.
     * @return The list of active machines belonging to the selected companies in the specified month and year.
     */
    protected List<Machine> getActiveMachinesByMonthAndYear(Integer selectedMonth, Integer selectedYear, List<Long> selectedCompanies) {
        return machineRepository.findByYearAndMonthAndCompanyIdInAndIsActive(selectedMonth, selectedYear, selectedCompanies, true);
    }


    /**
     * Deletes a machine part associated with the specified machine.
     *
     * @param machineId    The ID of the machine from which the part will be deleted.
     * @param machinePartId The ID of the machine part to be deleted.
     * @throws NotFoundException if the machine part with the specified ID is not associated with the specified machine.
     */
    @Override
    public void deleteMachinePart(Long machineId, Long machinePartId) {
        Machine machine = getMachineById(machineId);
        MachinePart machinePart = getMachinePartById(machinePartId);

        // Check if the machine part is associated with the machine
        if (!machine.getMachineParts().contains(machinePart)) {
            throw new NotFoundException("Machine part with id " + machinePartId + " is not associated with machine with id " + machineId);
        }
        // Remove the machine part from the machine's list of parts
        machine.getMachineParts().remove(machinePart);
        machinePartRepository.delete(machinePart);
        updateMachine(machine);
        log.info("MachineServiceImpl::deleteMachinePart - Machine part {} deleted successfully from {} machine!", machinePart.getPartName(), machine.getMachineBrand() + ' ' + machine.getMachineModel());
    }

    @Override
    public MachinePart getMachinePartById(Long partId) {
        return machinePartRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException("Machine part with id not found: " + partId));
    }


    /**
     * Converts images to base64 format for the given machine.
     *
     * @param machine The machine whose images will be converted.
     */
    @Override
    public void convertImagesToBase64(Machine machine) {
        for (Images image : machine.getImages()) {
//            We retrieve the binary value of the image and convert it to base64, then set it as the image's representation.
//            However, since we have saved both forms (base64 and binary, which is not recommended),
//            we simply retrieve the base64 value and set it for the image.

            byte[] imageData = image.getPicByte();
            if (imageData == null || imageData.length == 0) {
                throw new IllegalArgumentException("Image data is null or empty.");
            }
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            image.setBase64Image(base64Image);

            // Set the base64 representation of the image if it's already available
//            image.setBase64Image(image.getBase64Image());
        }
//        log.info("MachineServiceImpl::convertImagesToBase64 - ID of images belonging to the machine: " + machine.getImages().stream().map(Images::getId).toList());
    }


    /**
     * Retrieves a list of active machines for the specified year, month, and company IDs.
     * Only active machines are included in the result.
     *
     * @param year       The year for which to retrieve machines.
     * @param month      The month for which to retrieve machines.
     * @param companyIds The list of company IDs for which to retrieve machines.
     * @return A list of active machines for the specified criteria.
     */
    @Override
//    @Cacheable(value = "machines") does not update when a new machine is added, does not show the new one in between
    public List<Machine> getActiveMachinesByYearMonthAndCompanyId(int year, int month, List<Long> companyIds) {
//        log.info("MachineServiceImpl::getActiveMachinesByYearMonthAndCompanyId - CACHING");
        return machineRepository.findByYearAndMonthAndCompanyIdInAndIsActive(year, month, companyIds, true);
    }

//    get all deleted(inactivated) machines
    @Override
    public List<Machine> getDeletedMachines() {
        return machineRepository.findAllByRegistrationDateDeleted();
    }

}
