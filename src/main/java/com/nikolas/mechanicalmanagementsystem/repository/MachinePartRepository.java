package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.MachinePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface MachinePartRepository extends JpaRepository<MachinePart, Long> {
    @Query("SELECT SUM(mp.partPriceWithTax) FROM MachinePart mp WHERE mp.machine.id = :machineId")
    BigDecimal calculateTotalPartPriceByMachineId(@Param("machineId") Long machineId);

}
