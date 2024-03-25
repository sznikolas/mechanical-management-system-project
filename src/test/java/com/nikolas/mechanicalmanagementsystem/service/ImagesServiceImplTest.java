package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Images;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.ImagesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ImagesServiceImplTest {
    @InjectMocks
    private ImagesServiceImpl imagesServiceImpl;
    @Mock
    private ImagesRepository imagesRepository;
    @Mock
    private MachineService machineService;
    @Mock
    private static MultipartFile file1;

    private static Machine machine;
    private static Images image;

    @BeforeEach
    void setUp() {
        machine = new Machine();
        machine.setMachineBrand("TestBrand");
        machine.setMachineModel("TestModel");
        machine.setId(1L);

        image = new Images();
        image.setId(1L);
        image.setName("TestImage");
        image.setMachine(machine);

    }

    @Test
    void testUploadAndSaveImage_Successful() throws IOException {
        // Arrange
        List<MultipartFile> files = new ArrayList<>();

        when(file1.getOriginalFilename()).thenReturn("testImage1.jpg");
        when(file1.getContentType()).thenReturn("image/jpeg");
        when(file1.getBytes()).thenReturn(new byte[]{});
        when(file1.getSize()).thenReturn(1000L); // Smaller than MAX_IMAGE_SIZE
        files.add(file1);

        // Act & Assert
        assertDoesNotThrow(() -> imagesServiceImpl.uploadAndSaveImage(files, machine));

        // Verify
        verify(machineService, times(1)).saveMachine(machine);
    }

    @Test
    void testUploadAndSaveImage_ExceedMaxSize() {
        // Arrange
        List<MultipartFile> files = new ArrayList<>();

        when(file1.getSize()).thenReturn(10000000L); // Larger than MAX_IMAGE_SIZE
        files.add(file1);

        // Assert
        assertThrows(MaxUploadSizeExceededException.class, () -> imagesServiceImpl.uploadAndSaveImage(files, machine));
        // Verify
        verify(machineService, never()).saveMachine(machine);
    }

    @Test
    void testUploadAndSaveImage_InvalidType() {
        // Arrange
        List<MultipartFile> files = new ArrayList<>();

        when(file1.getContentType()).thenReturn("application/pdf"); // Not allowed image type
        files.add(file1);

        // Assert
        assertThrows(IOException.class, () -> imagesServiceImpl.uploadAndSaveImage(files, machine));

        // Verify
        verify(machineService, never()).saveMachine(machine);
    }


    @Test
    void testIsAllowedImageType() {
        // Arrange
        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        String[] notAllowedTypes = {"text/plain", "application/pdf", "image/bmp"};

        // Act and Assert
        for (String type : allowedTypes) {
            assertTrue(imagesServiceImpl.isAllowedImageType(type), type + " should be allowed");
        }

        for (String type : notAllowedTypes) {
            assertFalse(imagesServiceImpl.isAllowedImageType(type), type + " should not be allowed");
        }
    }

    @Test
    void testDeleteImage() {
        when(imagesRepository.findById(image.getId())).thenReturn(Optional.of(image));

        // Act
        assertDoesNotThrow(() -> imagesServiceImpl.deleteImage(image.getId()));

        // Assert
        verify(imagesRepository, times(1)).deleteById(image.getId());
    }

    @Test
    void testDeleteImage_NotFound() {
        when(imagesRepository.findById(image.getId())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> imagesServiceImpl.deleteImage(image.getId()));
        assertThrows(NotFoundException.class, () -> imagesServiceImpl.deleteImage(image.getId()));
        assertEquals("Image not found with id: " + image.getId(), exception.getMessage());

        // Verify
        verify(imagesRepository, never()).deleteById(anyLong());
    }

}