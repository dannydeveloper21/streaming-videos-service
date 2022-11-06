package com.javapoint.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.javapoint.exception.VideoStorageException;
import com.javapoint.model.UploadVideoResponse;
import com.javapoint.service.S3StorageService;
import com.javapoint.service.VideoStorageService;
import com.javapoint.service.VideoStreamingService;

import lombok.var;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("video")
public class VideoStreamingController {

	@Autowired
	private VideoStreamingService streamService;

	@Autowired
	private VideoStorageService videoStorageService;
	
	@Autowired S3StorageService s3Service;

	@GetMapping(value = "/{title}", produces = "video/mp4")
	public Mono<Resource> getVideo(@PathVariable String title, @RequestHeader("Range") String range) throws Exception {
	    try {
            return streamService.getVideo(title);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new Exception(e.getMessage());
        }
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadVideoResponse uploadVideo(@RequestParam("video") MultipartFile video) throws Exception {
		String videoName = null, videoDownloadUri = null;
//		if(video.getContentType() != "video/mp4") {
//		    throw new InvalidMediaTypeException("Video type not allowed, you must upload only mp4 videos type", videoName);
//		}
        try {
            if (s3Service.existsConnection()) {
               var response =  s3Service.putObject(video, true);
               return response;
                
            }else {
                videoName = videoStorageService.storeVideo(video);
                videoDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/download/")
                        .path(videoName)
                        .toString();
                return new UploadVideoResponse(videoName, videoDownloadUri, video.getContentType(), video.getSize());
            }           

        } catch (VideoStorageException e) {
            // TODO Auto-generated catch block
            throw new VideoStorageException(e.getMessage());
        }

	}
	
	@PostMapping(value = "/uploadMultipleVideos")
	public List<UploadVideoResponse> uploadMultipleVideos(@RequestParam("videos") MultipartFile [] videos){
	    return Arrays.asList(videos)
	            .stream()
	            .map(video -> {
                    try {
                        return uploadVideo(video);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                })
	            .collect(Collectors.toList());
	}
	
	@GetMapping("/download/{videoName:.+}")
	public ResponseEntity<Resource> downloadVideo(@PathVariable String videoName, HttpServletRequest request) throws Exception{
	 // Load file as Resource
        Resource resource = videoStorageService.loadFileAsResource(videoName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new IOException("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "video/mp4";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
	}
}
