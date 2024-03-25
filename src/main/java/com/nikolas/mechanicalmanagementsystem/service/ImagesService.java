package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImagesService {


//    void uploadAndSaveImage(MultipartFile file, Machine machine) throws IOException;
    void uploadAndSaveImage(List<MultipartFile> file, Machine machine) throws IOException;
    boolean isAllowedImageType(String contentType);
    void deleteImage(Long imageId);
}
