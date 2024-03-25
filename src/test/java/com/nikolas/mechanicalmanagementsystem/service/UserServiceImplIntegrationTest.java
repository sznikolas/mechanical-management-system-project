//package com.nikolas.mechanicalmanagementsystem.service;
//
//import com.nikolas.mechanicalmanagementsystem.entity.User;
//import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//@ActiveProfiles("test")
//@SpringBootTest
//public class UserServiceImplIntegrationTest {
//
//    @Autowired
//    private UserServiceImpl userServiceImpl;
//
//    @Test
//    void testFindById() {
//        // Fetch user from the test database
//        User originalUser = userServiceImpl.findById(2L)
//                .orElseThrow(() -> new NotFoundException("User not found with id: " + 2L));
//        System.out.println(originalUser.getFirstName());
//    }
//    @Test
//    public void testFindByEmail() {
//        User user = userServiceImpl.findByEmail("admin@gmail.com").orElseThrow(() -> new NotFoundException("No user found"));
//        System.out.println(user.getFirstName());
//    }
//}
