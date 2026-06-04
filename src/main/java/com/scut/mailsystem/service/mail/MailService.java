package com.scut.mailsystem.service.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.SendMailResponse;

public interface MailService {

    SendMailResponse sendMail(String authorizationHeader, SendMailRequest request);

    PageResult<MailListItemVO> getInbox(String authorizationHeader, Integer page, Integer size);

    PageResult<MailListItemVO> getSent(String authorizationHeader, Integer page, Integer size);
}
