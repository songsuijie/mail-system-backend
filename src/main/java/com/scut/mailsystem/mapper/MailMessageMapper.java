package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.mapper.row.MailListItemRow;
import com.scut.mailsystem.mapper.row.ThreadListItemRow;
import com.scut.mailsystem.mapper.row.ThreadMailRow;
import com.scut.mailsystem.mapper.row.ThreadReplyTextRow;
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

    long countSent(@Param("userId") Long userId,
                   @Param("keyword") String keyword,
                   @Param("recipientUsername") String recipientUsername,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime);

    List<MailListItemRow> selectSentPage(@Param("userId") Long userId,
                                         @Param("keyword") String keyword,
                                         @Param("recipientUsername") String recipientUsername,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("offset") Integer offset,
                                         @Param("size") Integer size);

    long countTrash(@Param("userId") Long userId,
                    @Param("keyword") String keyword,
                    @Param("startTime") String startTime,
                    @Param("endTime") String endTime);

    List<MailListItemRow> selectTrashPage(@Param("userId") Long userId,
                                          @Param("keyword") String keyword,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime,
                                          @Param("offset") Integer offset,
                                          @Param("size") Integer size);

    long countSpam(@Param("userId") Long userId,
                   @Param("keyword") String keyword,
                   @Param("spamLevel") String spamLevel,
                   @Param("riskLevel") String riskLevel,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime);

    List<MailListItemRow> selectSpamPage(@Param("userId") Long userId,
                                         @Param("keyword") String keyword,
                                         @Param("spamLevel") String spamLevel,
                                         @Param("riskLevel") String riskLevel,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("offset") Integer offset,
                                         @Param("size") Integer size);

    long countInboxUnread(@Param("userId") Long userId);

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

    long countThreadMailsAll(@Param("threadId") Long threadId);

    ThreadReplyTextRow selectLatestThreadReplyText(@Param("threadId") Long threadId,
                                                   @Param("userId") Long userId);
}
