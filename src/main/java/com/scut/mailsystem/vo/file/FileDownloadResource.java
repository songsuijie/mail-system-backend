package com.scut.mailsystem.vo.file;

import org.springframework.core.io.Resource;

public class FileDownloadResource {

    private final Resource resource;
    private final String originalFilename;
    private final String contentType;

    public FileDownloadResource(Resource resource, String originalFilename, String contentType) {
        this.resource = resource;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }
}
