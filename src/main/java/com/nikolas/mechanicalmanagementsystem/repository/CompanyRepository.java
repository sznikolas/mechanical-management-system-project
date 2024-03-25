package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}
