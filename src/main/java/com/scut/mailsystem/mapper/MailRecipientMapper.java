package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailRecipient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailRecipientMapper {

    int insert(MailRecipient mailRecipient);
}
