package com.scut.mailsystem.service.file;

import com.scut.mailsystem.vo.file.FileDownloadResource;
import com.scut.mailsystem.vo.file.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse uploadFile(String authorizationHeader, MultipartFile file);

    FileDownloadResource downloadFile(String authorizationHeader, String fileId);
}
