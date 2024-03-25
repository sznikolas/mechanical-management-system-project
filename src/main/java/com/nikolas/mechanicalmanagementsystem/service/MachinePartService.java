package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.entity.MachinePart;

public interface MachinePartService {

    void addMachinePartToMachine(Machine machine, MachinePart machinePart);

}
