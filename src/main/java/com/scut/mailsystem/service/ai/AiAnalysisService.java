package com.scut.mailsystem.service.ai;

import com.scut.mailsystem.entity.MailAnalysis;

import java.time.LocalDateTime;

public interface AiAnalysisService {

    MailAnalysis analyze(Long mailId, Long recipientId, String subject, String content, LocalDateTime now);
}
