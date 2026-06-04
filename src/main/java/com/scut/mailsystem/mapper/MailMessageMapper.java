package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.mapper.row.MailListItemRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MailMessageMapper {

    int insert(MailMessage mailMessage);

    long countInbox(@Param("userId") Long userId);

    List<MailListItemRow> selectInboxPage(@Param("userId") Long userId,
                                          @Param("offset") Integer offset,
                                          @Param("size") Integer size);

    long countSent(@Param("userId") Long userId);

    List<MailListItemRow> selectSentPage(@Param("userId") Long userId,
                                         @Param("offset") Integer offset,
                                         @Param("size") Integer size);

    MailDetailRow selectDetailByMailId(@Param("mailId") Long mailId);

    long countRelatedMailUser(@Param("mailId") Long mailId, @Param("userId") Long userId);
}
