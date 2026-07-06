package com.scut.mailsystem.controller.file;

import com.scut.mailsystem.service.file.FileService;
import com.scut.mailsystem.vo.file.FileDownloadResource;
import com.scut.mailsystem.vo.file.FileUploadResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest {

    private final FileService fileService = mock(FileService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new FileController(fileService))
            .build();

    @Test
    void uploadFile_acceptsMultipartFileAndReturnsFileId() throws Exception {
        when(fileService.uploadFile(eq("Bearer token"), any(MultipartFile.class)))
                .thenReturn(new FileUploadResponse("file_20260603_001"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.fileId").value("file_20260603_001"));

        ArgumentCaptor<MultipartFile> captor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(fileService).uploadFile(eq("Bearer token"), captor.capture());
        assertEquals("report.pdf", captor.getValue().getOriginalFilename());
        assertEquals("application/pdf", captor.getValue().getContentType());
    }

    @Test
    void downloadFile_returnsBinaryStreamWithAttachmentHeader() throws Exception {
        byte[] content = "file-content".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(content);
        when(fileService.downloadFile("Bearer token", "file_20260603_001"))
                .thenReturn(new FileDownloadResource(resource, "report.pdf", "application/pdf"));

        mockMvc.perform(get("/api/files/file_20260603_001/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("report.pdf")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(content));

        verify(fileService).downloadFile("Bearer token", "file_20260603_001");
    }
}
