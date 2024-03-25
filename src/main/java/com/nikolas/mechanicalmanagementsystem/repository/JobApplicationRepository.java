package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import com.nikolas.mechanicalmanagementsystem.entity.JobApplication;
import com.nikolas.mechanicalmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Optional<JobApplication> findByEmployeeAndCompany(User employee, Company company);
    void deleteAllByEmployeeAndCompany(User user, Company company);
    List<JobApplication> findByEmployee(User user);
}
