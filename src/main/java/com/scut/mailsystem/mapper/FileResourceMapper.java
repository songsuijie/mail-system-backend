package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.FileResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileResourceMapper {

    int insert(FileResource fileResource);

    FileResource selectByFileId(@Param("fileId") String fileId);

    int bindToMail(@Param("fileId") String fileId,
                   @Param("mailId") Long mailId,
                   @Param("status") String status);
}
