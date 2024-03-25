package com.nikolas.mechanicalmanagementsystem.service;

import com.nikolas.mechanicalmanagementsystem.entity.Images;
import com.nikolas.mechanicalmanagementsystem.entity.Machine;
import com.nikolas.mechanicalmanagementsystem.exception.NotFoundException;
import com.nikolas.mechanicalmanagementsystem.repository.ImagesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.nikolas.mechanicalmanagementsystem.entity.Images.MAX_IMAGE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImagesServiceImpl implements ImagesService {

    private final ImagesRepository imagesRepository;
    private final MachineService machineService;

    /**
     * Uploads and saves multiple images for a given machine.
     *
     * @param files   The list of MultipartFile objects representing the uploaded images.
     * @param machine The Machine object to which the images belong.
     * @throws IOException If an I/O exception occurs.
     */
    // MAX 5 MB image size
    @Override
    @Transactional
    public void uploadAndSaveImage(List<MultipartFile> files, Machine machine) throws IOException {
        try {
            // Check if the machine and files are valid
            if (machine != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    // Check if the file size exceeds the maximum allowed size
                    if (file.getSize() > MAX_IMAGE_SIZE) {
                        throw new MaxUploadSizeExceededException(MAX_IMAGE_SIZE);
                    }
                    // Check if the file type is allowed
                    if (!isAllowedImageType(file.getContentType())) {
                        throw new IOException("Invalid image type!");
                    }
                    // Create an Images object to store the image data
                    Images image = new Images();
                    image.setName(file.getOriginalFilename());
                    image.setType(file.getContentType());
                    image.setPicByte(file.getBytes());
                    image.setMachine(machine);
                    // Add the image to the machine's list of images
                    machine.getImages().add(image);
                    log.info("ImagesServiceImpl::uploadAndSaveImage - Image {} uploaded successfully to machine {}!", image.getName(), machine.getMachineBrand() + ' ' + machine.getMachineModel());
                }
                // Save the machine only once after adding all images
                machineService.saveMachine(machine);
            }
        } catch (IOException | MaxUploadSizeExceededException e) {
            log.error("ImagesServiceImpl::uploadAndSaveImage - Error: " + e.getMessage());
            throw e;
        }
    }


    /**
     * Checks if the given image content type is allowed for upload.
     *
     * @param contentType The content type of the image.
     * @return true if the content type is allowed, false otherwise.
     */
    public boolean isAllowedImageType(String contentType) {
        // List of allowed image types
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp"); // Add more allowed types
        return allowedTypes.contains(contentType);
    }


    /**
     * Deletes the image with the given ID.
     *
     * @param imageId The ID of the image to delete.
     * @throws NotFoundException if the image with the given ID is not found.
     */
    @Override
    public void deleteImage(Long imageId) {
        Images image = imagesRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));
        imagesRepository.deleteById(imageId);
        log.info("ImagesServiceImpl::deleteImage - {} image removed from machine {}!", image.getName(),
                image.getMachine().getMachineBrand() + ' ' + image.getMachine().getMachineModel());
    }

}
