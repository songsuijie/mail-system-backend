package com.scut.mailsystem.service.ai;

import com.scut.mailsystem.entity.MailAnalysis;

import java.time.LocalDateTime;

public interface RuleAnalysisService {

    MailAnalysis analyze(Long mailId, Long recipientId, String subject, String content, LocalDateTime now);

    MailAnalysis defaultAnalysis(Long mailId, Long recipientId, LocalDateTime now);
}
