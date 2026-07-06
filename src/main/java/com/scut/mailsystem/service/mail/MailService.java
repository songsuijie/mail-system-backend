package com.scut.mailsystem.service.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.MailStatisticsVO;
import com.scut.mailsystem.vo.mail.RestoreMailResponse;
import com.scut.mailsystem.vo.mail.RetryAnalysisResponse;
import com.scut.mailsystem.vo.mail.SendEmailData;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import com.scut.mailsystem.vo.mail.ThreadDetailVO;
import com.scut.mailsystem.vo.mail.ThreadListItemVO;

public interface MailService {

    SendMailResponse sendMail(String authorizationHeader, SendMailRequest request);

    SendEmailData sendEmail(String authorizationHeader, SendEmailRequest request);

    SendEmailData replyEmail(String authorizationHeader, ReplyEmailRequest request);

    PageResult<MailListItemVO> getInbox(String authorizationHeader, Integer page, Integer size);

    PageResult<MailListItemVO> getSent(String authorizationHeader,
                                       Integer page,
                                       Integer size,
                                       String keyword,
                                       String recipientUsername,
                                       String startTime,
                                       String endTime);

    MailDetailVO getMailDetail(String authorizationHeader, Long mailId);

    MailReadResponse markRead(String authorizationHeader, Long mailId);

    MailDeleteResponse deleteMail(String authorizationHeader, Long mailId);

    PageResult<MailListItemVO> getTrash(String authorizationHeader,
                                        Integer page,
                                        Integer size,
                                        String keyword,
                                        String startTime,
                                        String endTime);

    PageResult<MailListItemVO> getSpam(String authorizationHeader,
                                       Integer page,
                                       Integer size,
                                       String keyword,
                                       String spamLevel,
                                       String riskLevel,
                                       String startTime,
                                       String endTime);

    MailStatisticsVO getStatistics(String authorizationHeader);

    RestoreMailResponse restoreMail(String authorizationHeader, Long mailId);

    RetryAnalysisResponse retryAnalysis(String authorizationHeader, Long mailId);

    PageResult<ThreadListItemVO> getThreads(String authorizationHeader,
                                            Integer page,
                                            Integer size,
                                            String keyword,
                                            String readStatus,
                                            String senderUsername,
                                            String priority,
                                            String startTime,
                                            String endTime);

    ThreadDetailVO getThreadDetail(String authorizationHeader, Long threadId, String cursor, Integer limit);
}
