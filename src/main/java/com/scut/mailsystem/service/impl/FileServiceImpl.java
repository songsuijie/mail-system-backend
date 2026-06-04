package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.entity.FileResource;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.FileResourceMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.file.FileService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.file.FileDownloadResource;
import com.scut.mailsystem.vo.file.FileUploadResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String STATUS_UPLOADED = "UPLOADED";
    private static final Path STORAGE_ROOT = Path.of("uploads", "attachments");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf", "docx", "zip");

    private final SysUserMapper sysUserMapper;
    private final FileResourceMapper fileResourceMapper;
    private final MailMessageMapper mailMessageMapper;

    public FileServiceImpl(SysUserMapper sysUserMapper,
                           FileResourceMapper fileResourceMapper,
                           MailMessageMapper mailMessageMapper) {
        this.sysUserMapper = sysUserMapper;
        this.fileResourceMapper = fileResourceMapper;
        this.mailMessageMapper = mailMessageMapper;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(String authorizationHeader, MultipartFile file) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小不能超过 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = getAllowedFileExt(originalFilename);
        String fileId = "file_" + UUID.randomUUID().toString().replace("-", "");
        String storedFilename = fileId + "." + fileExt;
        Path storagePath = STORAGE_ROOT.resolve(storedFilename).normalize();

        try {
            Files.createDirectories(STORAGE_ROOT);
            file.transferTo(storagePath);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

        LocalDateTime now = LocalDateTime.now();
        FileResource fileResource = new FileResource();
        fileResource.setFileId(fileId);
        fileResource.setUploaderId(currentUser.getId());
        fileResource.setMailId(null);
        fileResource.setOriginalFilename(originalFilename);
        fileResource.setStoredFilename(storedFilename);
        fileResource.setStoragePath(storagePath.toString());
        fileResource.setContentType(defaultContentType(file.getContentType()));
        fileResource.setFileExt(fileExt);
        fileResource.setFileSize(file.getSize());
        fileResource.setStatus(STATUS_UPLOADED);
        fileResource.setCreatedAt(now);
        fileResource.setUpdatedAt(now);
        fileResourceMapper.insert(fileResource);

        return new FileUploadResponse(fileId);
    }

    @Override
    public FileDownloadResource downloadFile(String authorizationHeader, String fileId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        if (!StringUtils.hasText(fileId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }

        FileResource fileResource = fileResourceMapper.selectByFileId(fileId);
        if (fileResource == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        assertDownloadPermission(currentUser, fileResource);

        Path storagePath = Path.of(fileResource.getStoragePath()).normalize();
        Resource resource = new FileSystemResource(storagePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }

        return new FileDownloadResource(
                resource,
                fileResource.getOriginalFilename(),
                defaultContentType(fileResource.getContentType())
        );
    }

    private void assertDownloadPermission(SysUser currentUser, FileResource fileResource) {
        if (fileResource.getMailId() == null) {
            if (!currentUser.getId().equals(fileResource.getUploaderId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权限下载该文件");
            }
            return;
        }

        long relatedCount = mailMessageMapper.countRelatedMailUser(fileResource.getMailId(), currentUser.getId());
        if (relatedCount <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限下载该文件");
        }
    }

    private SysUser getCurrentActiveUser(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        LoginUser loginUser = TokenUtils.parseToken(token);
        SysUser currentUser = sysUserMapper.selectActiveById(loginUser.getUserId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return currentUser;
    }

    private String extractToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return AuthHeaderUtils.extractToken(authorizationHeader);
    }

    private String getAllowedFileExt(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型不支持");
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型不支持");
        }

        String fileExt = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(fileExt)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型不支持");
        }
        return fileExt;
    }

    private String defaultContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }
}
