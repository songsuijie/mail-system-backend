package com.scut.mailsystem.service.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.SendEmailData;
import com.scut.mailsystem.vo.mail.SendMailResponse;

public interface MailService {

    SendMailResponse sendMail(String authorizationHeader, SendMailRequest request);

    SendEmailData sendEmail(String authorizationHeader, SendEmailRequest request);

    SendEmailData replyEmail(String authorizationHeader, ReplyEmailRequest request);

    PageResult<MailListItemVO> getInbox(String authorizationHeader, Integer page, Integer size);

    PageResult<MailListItemVO> getSent(String authorizationHeader, Integer page, Integer size);

    MailDetailVO getMailDetail(String authorizationHeader, Long mailId);

    MailReadResponse markRead(String authorizationHeader, Long mailId);

    MailDeleteResponse deleteMail(String authorizationHeader, Long mailId);
}
