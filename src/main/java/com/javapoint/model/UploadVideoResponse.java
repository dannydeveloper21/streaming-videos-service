package com.javapoint.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UploadVideoResponse {

    private String videoName;
    private String videoDownloadUri;
    private String contentType;
    private long size;

    
}

