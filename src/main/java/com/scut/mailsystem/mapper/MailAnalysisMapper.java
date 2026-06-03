package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailAnalysis;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailAnalysisMapper {

    int insert(MailAnalysis mailAnalysis);
}
