package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.MachinePart;
import com.nikolas.mechanicalmanagementsystem.repository.MachineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MachinePartServiceImplTest {
    @InjectMocks
    private MachinePartServiceImpl machinePartServiceImpl;
    @Mock
    private MachineRepository machineRepository;
    @Mock
    private MachineService machineService;

    @Test
    void testAddMachinePartToMachine() {
        // Arrange
        Machine machine = new Machine();
        machine.setMachineBrand("Renault");
        machine.setMachineModel("Megane");
        machine.setTaxInPercent(new BigDecimal("20"));

        MachinePart machinePart = new MachinePart();
        machinePart.setPartName("Test Part");
        machinePart.setPartPrice(new BigDecimal("100"));
        when(machineRepository.save(any())).thenReturn(machine);

        // Act
        machinePartServiceImpl.addMachinePartToMachine(machine, machinePart);

        // Assert
        verify(machineRepository, times(1)).save(any(Machine.class));
        verify(machineService, times(1)).updateMachine(machine);

        // Verify the new machine part details
        assertEquals(1, machine.getMachineParts().size());
        MachinePart newMachinePart = machine.getMachineParts().get(0);
        assertEquals("Test Part", newMachinePart.getPartName());
        assertEquals(new BigDecimal("100"), newMachinePart.getPartPrice());
        assertEquals(new BigDecimal("20"), newMachinePart.getTaxInPercent());
        assertEquals(new BigDecimal("20.00"), newMachinePart.getUnitTax());
        assertEquals(new BigDecimal("120.00"), newMachinePart.getPartPriceWithTax());
    }

}