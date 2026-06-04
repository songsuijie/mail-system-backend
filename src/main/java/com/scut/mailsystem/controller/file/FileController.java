package com.scut.mailsystem.controller.file;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.service.file.FileService;
import com.scut.mailsystem.vo.file.FileDownloadResource;
import com.scut.mailsystem.vo.file.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(fileService.uploadFile(authorizationHeader, file));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("fileId") String fileId) {
        FileDownloadResource downloadResource = fileService.downloadFile(authorizationHeader, fileId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadResource.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(downloadResource.getResource());
    }
}
