package com.javapoint.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.javapoint.config.VideoStorageProperties;
import com.javapoint.exception.VideoStorageException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VideoStorageService {
    
    private final Path videoStorageLocation;
    
    @Autowired
    public VideoStorageService(VideoStorageProperties videoStorageProperties) throws VideoStorageException{
        this.videoStorageLocation = Paths.get(videoStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.videoStorageLocation);
        } catch (Exception ex) {
            log.info("Could not create the directory where the uploaded videos will be stored.");
            throw new VideoStorageException("Could not create the directory where the uploaded videos will be stored.", ex);
        }
    }
    
    public String storeVideo(MultipartFile video) throws VideoStorageException {
     // Normalize file name
        String videoName = StringUtils.cleanPath(video.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(videoName.contains("..")) {
                log.info("Sorry! Filename contains invalid path sequence ");
                throw new VideoStorageException("Sorry! Filename contains invalid path sequence " + videoName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.videoStorageLocation.resolve(videoName);
            Files.copy(video.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return videoName;
        } catch (IOException ex) {
            log.info("Could not store file " + videoName + ". Please try again!");
            throw new VideoStorageException("Could not store file " + videoName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String videoName) {
        try {
            Path filePath = this.videoStorageLocation.resolve(videoName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new VideoStorageException("File not found " + videoName);
            }
        } catch (MalformedURLException ex) {
            throw new VideoStorageException("File not found " + videoName, ex);
        }
    }

}
