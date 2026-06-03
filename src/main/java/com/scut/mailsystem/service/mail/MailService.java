package com.scut.mailsystem.service.mail;

import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.vo.mail.SendMailResponse;

public interface MailService {

    SendMailResponse sendMail(String authorizationHeader, SendMailRequest request);
}
