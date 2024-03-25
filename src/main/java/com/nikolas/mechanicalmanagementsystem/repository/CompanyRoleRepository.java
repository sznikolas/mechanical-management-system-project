package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRoleRepository extends JpaRepository<CompanyRole, Long> {

}
