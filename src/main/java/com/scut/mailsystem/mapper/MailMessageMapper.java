package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.mapper.row.MailListItemRow;
import com.scut.mailsystem.mapper.row.ThreadListItemRow;
import com.scut.mailsystem.mapper.row.ThreadMailRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MailMessageMapper {

    int insert(MailMessage mailMessage);

    int updateThreadFields(@Param("mailId") Long mailId,
                           @Param("threadId") Long threadId,
                           @Param("replyToMailId") Long replyToMailId);

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

    long countThreads(@Param("userId") Long userId,
                      @Param("keyword") String keyword,
                      @Param("readStatus") String readStatus,
                      @Param("senderUsername") String senderUsername,
                      @Param("priority") String priority,
                      @Param("startTime") String startTime,
                      @Param("endTime") String endTime);

    List<ThreadListItemRow> selectThreadPage(@Param("userId") Long userId,
                                             @Param("keyword") String keyword,
                                             @Param("readStatus") String readStatus,
                                             @Param("senderUsername") String senderUsername,
                                             @Param("priority") String priority,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime,
                                             @Param("offset") Integer offset,
                                             @Param("size") Integer size);

    long countThreadMails(@Param("threadId") Long threadId, @Param("userId") Long userId);

    List<ThreadMailRow> selectThreadMails(@Param("threadId") Long threadId,
                                          @Param("userId") Long userId,
                                          @Param("limit") Integer limit);
}
