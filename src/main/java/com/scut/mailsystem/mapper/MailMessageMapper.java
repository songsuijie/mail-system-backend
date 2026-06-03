package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailMessageMapper {

    int insert(MailMessage mailMessage);
}
