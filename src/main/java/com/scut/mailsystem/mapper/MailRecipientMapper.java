package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailRecipient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface MailRecipientMapper {

    int insert(MailRecipient mailRecipient);

    int markReadIfUnread(@Param("mailId") Long mailId,
                         @Param("recipientId") Long recipientId,
                         @Param("readAt") LocalDateTime readAt);

    int deleteRecipientMailIfNotDeleted(@Param("mailId") Long mailId,
                                        @Param("recipientId") Long recipientId,
                                        @Param("deletedAt") LocalDateTime deletedAt);

    int restoreRecipientMailIfDeleted(@Param("mailId") Long mailId,
                                      @Param("recipientId") Long recipientId);
}
