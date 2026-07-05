package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.entity.FileResource;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.FileResourceMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.file.FileDownloadResource;
import com.scut.mailsystem.vo.file.FileUploadResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileServiceImplTest {

    private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
    private final FileResourceMapper fileResourceMapper = mock(FileResourceMapper.class);
    private final MailMessageMapper mailMessageMapper = mock(MailMessageMapper.class);
    private final FileServiceImpl fileService = new FileServiceImpl(
            sysUserMapper,
            fileResourceMapper,
            mailMessageMapper
    );

    @TempDir
    private Path tempDir;

    @AfterEach
    void cleanGeneratedUploads() throws IOException {
        Path uploads = Path.of("uploads");
        if (!Files.exists(uploads)) {
            return;
        }
        try (var paths = Files.walk(uploads)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup for files created by MockMultipartFile.transferTo.
                        }
                    });
        }
    }

    @Test
    void uploadFile_withAllowedPdfStoresMetadataAndReturnsFileId() {
        SysUser alice = activeUser(1L, "alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        MockMultipartFile file = multipartFile("report.pdf", "application/pdf", "hello".getBytes());

        FileUploadResponse response = fileService.uploadFile(authHeader(1L, "alice"), file);

        assertNotNull(response.getFileId());
        assertTrue(response.getFileId().startsWith("file_"));
        ArgumentCaptor<FileResource> captor = ArgumentCaptor.forClass(FileResource.class);
        verify(fileResourceMapper).insert(captor.capture());
        FileResource saved = captor.getValue();
        assertEquals(response.getFileId(), saved.getFileId());
        assertEquals(1L, saved.getUploaderId());
        assertEquals("report.pdf", saved.getOriginalFilename());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals("pdf", saved.getFileExt());
        assertEquals(5L, saved.getFileSize());
        assertEquals("UPLOADED", saved.getStatus());
    }

    @Test
    void uploadFile_rejectsEmptyFile() {
        when(sysUserMapper.selectActiveById(1L)).thenReturn(activeUser(1L, "alice"));
        MockMultipartFile file = multipartFile("empty.pdf", "application/pdf", new byte[0]);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.uploadFile(authHeader(1L, "alice"), file)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void uploadFile_rejectsUnsupportedFileType() {
        when(sysUserMapper.selectActiveById(1L)).thenReturn(activeUser(1L, "alice"));
        MockMultipartFile file = multipartFile("archive.7z", "application/x-7z-compressed", "data".getBytes());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.uploadFile(authHeader(1L, "alice"), file)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void uploadFile_rejectsFileLargerThanTenMb() {
        when(sysUserMapper.selectActiveById(1L)).thenReturn(activeUser(1L, "alice"));
        MockMultipartFile file = multipartFile("large.pdf", "application/pdf", new byte[10 * 1024 * 1024 + 1]);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.uploadFile(authHeader(1L, "alice"), file)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void downloadFile_withoutTokenReturnsUnauthorized() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.downloadFile(null, "file_001")
        );

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
    }

    @Test
    void downloadFile_forUnboundFileAllowsOnlyUploader() throws IOException {
        when(sysUserMapper.selectActiveById(1L)).thenReturn(activeUser(1L, "alice"));
        FileResource file = fileResource("file_001", 1L, null, existingTempFile());
        when(fileResourceMapper.selectByFileId("file_001")).thenReturn(file);

        FileDownloadResource resource = fileService.downloadFile(authHeader(1L, "alice"), "file_001");

        assertEquals("report.pdf", resource.getOriginalFilename());
        assertEquals("application/pdf", resource.getContentType());
        assertTrue(resource.getResource().exists());
    }

    @Test
    void downloadFile_forUnboundFileRejectsOtherUser() throws IOException {
        when(sysUserMapper.selectActiveById(2L)).thenReturn(activeUser(2L, "bob"));
        FileResource file = fileResource("file_001", 1L, null, existingTempFile());
        when(fileResourceMapper.selectByFileId("file_001")).thenReturn(file);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.downloadFile(authHeader(2L, "bob"), "file_001")
        );

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void downloadFile_forBoundFileAllowsRelatedMailUser() throws IOException {
        when(sysUserMapper.selectActiveById(2L)).thenReturn(activeUser(2L, "bob"));
        FileResource file = fileResource("file_001", 1L, 100L, existingTempFile());
        when(fileResourceMapper.selectByFileId("file_001")).thenReturn(file);
        when(mailMessageMapper.countRelatedMailUser(100L, 2L)).thenReturn(1L);

        FileDownloadResource resource = fileService.downloadFile(authHeader(2L, "bob"), "file_001");

        assertEquals("report.pdf", resource.getOriginalFilename());
        assertTrue(resource.getResource().exists());
    }

    @Test
    void downloadFile_forBoundFileRejectsUnrelatedUser() throws IOException {
        when(sysUserMapper.selectActiveById(3L)).thenReturn(activeUser(3L, "charlie"));
        FileResource file = fileResource("file_001", 1L, 100L, existingTempFile());
        when(fileResourceMapper.selectByFileId("file_001")).thenReturn(file);
        when(mailMessageMapper.countRelatedMailUser(100L, 3L)).thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.downloadFile(authHeader(3L, "charlie"), "file_001")
        );

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    private MockMultipartFile multipartFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private FileResource fileResource(String fileId, Long uploaderId, Long mailId, Path storagePath) {
        FileResource file = new FileResource();
        file.setFileId(fileId);
        file.setUploaderId(uploaderId);
        file.setMailId(mailId);
        file.setOriginalFilename("report.pdf");
        file.setStoredFilename(fileId + ".pdf");
        file.setStoragePath(storagePath.toString());
        file.setContentType("application/pdf");
        file.setFileExt("pdf");
        file.setFileSize(5L);
        file.setStatus(mailId == null ? "UPLOADED" : "BOUND");
        return file;
    }

    private Path existingTempFile() throws IOException {
        Path file = tempDir.resolve("report.pdf");
        Files.writeString(file, "hello");
        return file;
    }

    private SysUser activeUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        return user;
    }

    private String authHeader(Long userId, String username) {
        return "Bearer " + TokenUtils.generateToken(userId, username);
    }
}
