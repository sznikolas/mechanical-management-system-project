package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.MachinePart;
import com.nikolas.mechanicalmanagementsystem.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@RequiredArgsConstructor
@Service
public class MachinePartServiceImpl implements MachinePartService {

    private final MachineRepository machineRepository;
    private final MachineService machineService;

    /**
     * Adds a machine part to the specified machine.
     *
     * @param machine     The machine to which the part will be added.
     * @param machinePart The machine part to be added.
     */
    @Override
    public void addMachinePartToMachine(Machine machine, MachinePart machinePart) {

        // Create a new machine part instance with the provided details
        MachinePart newMachinePart = new MachinePart();
        newMachinePart.setMachine(machine);
        newMachinePart.setPartName(machinePart.getPartName());
        newMachinePart.setPartPrice(machinePart.getPartPrice());

        // Calculate tax-related details for the new machine part
        newMachinePart.setTaxInPercent(machine.getTaxInPercent());
        newMachinePart.setUnitTax(machinePart.getPartPrice().multiply(machine.getTaxInPercent().divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN)));
        newMachinePart.setPartPriceWithTax(newMachinePart.getUnitTax().add(machinePart.getPartPrice()));

        // Add the new machine part to the machine's list of parts
        machine.getMachineParts().add(newMachinePart);
        log.info("MachinePartServiceImpl::addMachinePartToMachine - {} machine part added to {} machine", machinePart.getPartName(), machine.getMachineBrand() + ' ' + machine.getMachineModel());

        // Save the machine with the new part
        machineRepository.save(machine);
        machineService.updateMachine(machine);

    }

}
