package com.javapoint.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VideoStreamingService {

	private static final String FORMAT = "classpath:mp4/%s.mp4";

	@Autowired
	private ResourceLoader loader;
	
	@Autowired
	private S3StorageService s3Service;

	public Mono<Resource> getVideo(String video) throws Exception {
	    try {
	        if (s3Service.existsConnection()) {
	            S3Object obj = s3Service.getObject(video);
	            S3ObjectInputStream resource = obj.getObjectContent();
	            InputStream stream = resource.getDelegateStream();
	            log.info("Video {} loaded",obj.getKey());
	            return Mono.fromSupplier(()-> new InputStreamResource(stream));
	        }
	        return Mono.fromSupplier(() -> this.loader.getResource(String.format(FORMAT, video)));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
	}

}
