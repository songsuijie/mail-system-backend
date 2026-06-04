package com.scut.mailsystem.vo.file;

public class FileUploadResponse {

    private String fileId;

    public FileUploadResponse() {
    }

    public FileUploadResponse(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
