package com.javapoint.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.javapoint.model.UploadVideoResponse;

import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class S3StorageService {
    
    @Value("${aws.bucket}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3Client;
    
    public boolean existsConnection() {
        return !amazonS3Client.equals(null);
    }
    
    private void createS3Bucket() {
        if (amazonS3Client.doesBucketExistV2(bucketName)) {
            log.info("Bucket name already in use.");
            return;
        }
        
        amazonS3Client.createBucket(bucketName);
    }
    
    public UploadVideoResponse putObject(MultipartFile video, boolean publicObject) throws Exception {

        createS3Bucket();
        
        String objectName = StringUtils.cleanPath(video.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(video.getSize());
        metadata.setContentType(video.getContentType());

        try {
            
            var putObjectRequest = new PutObjectRequest(bucketName, objectName, video.getInputStream(),metadata).withCannedAcl(
                    (publicObject)? CannedAccessControlList.PublicRead:CannedAccessControlList.Private);
            amazonS3Client.putObject(putObjectRequest);
            S3Object obj = amazonS3Client.getObject(bucketName,objectName);
            return new UploadVideoResponse(obj.getKey(),obj.getRedirectLocation(),video.getContentType(),video.getSize());
        } catch (Exception e){
            log.error("Some error has ocurred.");
            throw new AmazonS3Exception(e.getMessage());
        }

    }
    
    public S3Object getObject(String video) {
        String objectName = (video.contains(".mp4")? video: video+".mp4");
        return amazonS3Client.getObject(bucketName,objectName);
    }
    
    public List<S3ObjectSummary> listObjects(String bucketName){
        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        return objectListing.getObjectSummaries();
    }
    
    public void downloadObject(String bucketName, String objectName){
        S3Object s3object = amazonS3Client.getObject(bucketName, objectName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File("." + File.separator + objectName));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    public void deleteObject(String bucketName, String objectName){
        amazonS3Client.deleteObject(bucketName, objectName);
    }
}
