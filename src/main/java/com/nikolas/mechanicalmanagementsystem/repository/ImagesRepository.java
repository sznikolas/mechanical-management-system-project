package com.nikolas.mechanicalmanagementsystem.repository;

import com.nikolas.mechanicalmanagementsystem.entity.Images;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {


}