package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    @Query("SELECT m FROM Machine m WHERE m.isActive = false")
    List<Machine> findAllByRegistrationDateDeleted();

    @Modifying
    @Query(value = "UPDATE Machine SET isActive = false WHERE id = :machineId")
    void deactivateMachine(@Param("machineId") Long machineId);

//------------------
    default List<Machine> findByYearAndMonthAndCompanyIdInAndIsActive(Integer year, Integer month, List<Long> companies, boolean isActive) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return findByRegistrationDateBetweenAndCompanyIdInAndIsActive(startOfMonth, endOfMonth, companies, isActive);
    }
    //Spring Data JPA "query derivation"
//    List<Machine> findByRegistrationDateBetweenAndCompanyIdInAndIsActive(LocalDateTime start, LocalDateTime end, List<Long> companies, boolean isActive);
    List<Machine> findByRegistrationDateBetweenAndCompanyIdInAndIsActive(LocalDateTime start, LocalDateTime end, List<Long> companies, boolean isActive);
}
