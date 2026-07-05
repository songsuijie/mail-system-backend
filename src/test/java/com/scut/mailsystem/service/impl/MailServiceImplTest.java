package com.scut.mailsystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.entity.FileResource;
import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.entity.MailRecipient;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.mapper.FileResourceMapper;
import com.scut.mailsystem.mapper.MailAnalysisMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.MailRecipientMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.SendEmailData;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailServiceImplTest {

    private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
    private final MailMessageMapper mailMessageMapper = mock(MailMessageMapper.class);
    private final MailRecipientMapper mailRecipientMapper = mock(MailRecipientMapper.class);
    private final MailAnalysisMapper mailAnalysisMapper = mock(MailAnalysisMapper.class);
    private final FileResourceMapper fileResourceMapper = mock(FileResourceMapper.class);
    private final MailServiceImpl mailService = new MailServiceImpl(
            sysUserMapper,
            mailMessageMapper,
            mailRecipientMapper,
            mailAnalysisMapper,
            fileResourceMapper,
            new ObjectMapper()
    );

    @Test
    void sendEmail_createsNewThreadAndReturnsMailIdAndThreadId() {
        SysUser alice = activeUser(1L, "alice", "Alice");
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(sysUserMapper.selectActiveByUsername("bob")).thenReturn(bob);
        when(mailMessageMapper.insert(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, MailMessage.class).setId(101L);
            return 1;
        });

        SendEmailData response = mailService.sendEmail(
                authHeader(1L, "alice"),
                sendEmailRequest("bob", "实验报告提交提醒", "请查收附件中的实验报告。", null)
        );

        assertEquals(101L, response.getMailId());
        assertEquals(101L, response.getThreadId());
        ArgumentCaptor<MailMessage> messageCaptor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailMessageMapper).insert(messageCaptor.capture());
        MailMessage inserted = messageCaptor.getValue();
        assertEquals("实验报告提交提醒", inserted.getSubject());
        assertNull(inserted.getThreadId());
        assertNull(inserted.getReplyToMailId());
        verify(mailMessageMapper).updateThreadFields(101L, 101L, null);
    }

    @Test
    void replyEmail_createsReplyInExistingThreadAndTargetsOriginalSender() {
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(2L)).thenReturn(bob);
        MailDetailRow original = detailRow(100L, 1L, 2L, 1, 0);
        original.setThreadId(2001L);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(original);
        when(mailMessageMapper.insert(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, MailMessage.class).setId(102L);
            return 1;
        });

        SendEmailData response = mailService.replyEmail(
                authHeader(2L, "bob"),
                replyEmailRequest(100L, 2001L, "Re: 实验报告提交提醒", "已收到，我会尽快查看。")
        );

        assertEquals(102L, response.getMailId());
        assertEquals(2001L, response.getThreadId());
        ArgumentCaptor<MailMessage> messageCaptor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailMessageMapper).insert(messageCaptor.capture());
        MailMessage inserted = messageCaptor.getValue();
        assertEquals(2L, inserted.getSenderId());
        assertEquals(2001L, inserted.getThreadId());
        assertEquals(100L, inserted.getReplyToMailId());

        ArgumentCaptor<MailRecipient> recipientCaptor = ArgumentCaptor.forClass(MailRecipient.class);
        verify(mailRecipientMapper).insert(recipientCaptor.capture());
        assertEquals(1L, recipientCaptor.getValue().getRecipientId());
        verify(mailMessageMapper, never()).updateThreadFields(eq(102L), any(), any());
    }

    @Test
    void getMailDetail_recipientUnread_autoMarksReadAndReturnsRichTextContent() {
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(2L)).thenReturn(bob);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(detailRow(100L, 1L, 2L, 0, 0));

        MailDetailVO detail = mailService.getMailDetail(authHeader(2L, "bob"), 100L);

        assertEquals(100L, detail.getMailId());
        assertEquals("RECIPIENT", detail.getCurrentUserRole());
        assertTrue(detail.getRead());
        assertEquals("paragraph", ((Map<?, ?>) detail.getContent().get(0)).get("type"));
        assertNotNull(detail.getAnalysis());
        assertEquals("SUCCESS", detail.getAnalysis().getAnalysisStatus());
        assertEquals(List.of("好的，我会按时提交。"), detail.getAnalysis().getReplySuggestions());
        assertNull(detail.getAttachment());
        verify(mailRecipientMapper).markReadIfUnread(eq(100L), eq(2L), any(LocalDateTime.class));
    }

    @Test
    void sendMail_insertsSuccessAnalysisWithSummaryAndReplySuggestions() {
        SendMailCapture capture = sendMailAndCaptureAnalysis("普通通知", "请提交报告");
        SendMailResponse response = capture.response();
        MailAnalysis analysis = capture.analysis();

        assertEquals("SUCCESS", response.getAnalysisStatus());
        assertEquals("SUCCESS", analysis.getAnalysisStatus());
        assertEquals("请提交报告", analysis.getSummary());
        assertNotNull(analysis.getReplySuggestions());
        assertTrue(analysis.getReplySuggestions().contains("收到，我会尽快处理。"));
    }

    @Test
    void sendMail_withDeadlineKeyword_setsHighPriority() {
        MailAnalysis analysis = sendMailAndCaptureAnalysis("紧急通知", "请在今天截止前提交材料").analysis();

        assertEquals("HIGH", analysis.getPriority());
        assertTrue(analysis.getPriorityScore() >= 80);
        assertTrue(analysis.getPriorityReason().contains("截止"));
    }

    @Test
    void sendMail_withSpamKeywords_setsSpamFlagAndLevel() {
        MailAnalysis analysis = sendMailAndCaptureAnalysis("中奖通知", "点击领取优惠券，免费领取返现礼包").analysis();

        assertEquals(1, analysis.getSpamFlag());
        assertTrue(List.of("LOW", "MEDIUM", "HIGH").contains(analysis.getSpamLevel()));
        assertTrue(analysis.getSpamScore() > 0);
    }

    @Test
    void sendMail_withGiftCardPaymentSignal_setsSpamAndRiskReason() {
        MailAnalysis analysis = sendMailAndCaptureAnalysis("紧急付款", "请立即购买 Apple gift card 并发送 PIN").analysis();

        assertEquals(1, analysis.getSpamFlag());
        assertTrue(List.of("LOW", "MEDIUM", "HIGH").contains(analysis.getSpamLevel()));
        assertTrue(List.of("MEDIUM", "HIGH").contains(analysis.getRiskLevel()));
        assertTrue(analysis.getSpamReason().contains("gift card"));
        assertTrue(analysis.getRiskReason().contains("gift card"));
    }

    @Test
    void sendMail_withRiskKeywords_setsRiskLevel() {
        MailAnalysis analysis = sendMailAndCaptureAnalysis(
                "账号异常",
                "请立即登录 http://example.com 或 t.cn/abc 修改密码并输入验证码"
        ).analysis();

        assertTrue(List.of("MEDIUM", "HIGH").contains(analysis.getRiskLevel()));
        assertTrue(analysis.getRiskScore() > 0);
        assertNotNull(analysis.getRiskReason());
        assertTrue(analysis.getRiskReason().contains("t.cn"));
    }

    @Test
    void sendMail_withAttachment_bindsUploadedFileToCreatedMail() {
        SysUser alice = activeUser(1L, "alice", "Alice");
        SysUser bob = activeUser(2L, "bob", "Bob");
        FileResource file = uploadedFile("file_test_001", 1L);
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(sysUserMapper.selectActiveByUsername("bob")).thenReturn(bob);
        when(fileResourceMapper.selectByFileId("file_test_001")).thenReturn(file);
        when(mailMessageMapper.insert(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.scut.mailsystem.entity.MailMessage.class).setId(100L);
            return 1;
        });
        when(fileResourceMapper.bindToMail("file_test_001", 100L, "BOUND")).thenReturn(1);

        SendMailRequest request = sendMailRequest("bob", "file_test_001");
        SendMailResponse response = mailService.sendMail(authHeader(1L, "alice"), request);

        assertEquals(100L, response.getMailId());
        assertEquals("SUCCESS", response.getAnalysisStatus());
        verify(fileResourceMapper).bindToMail("file_test_001", 100L, "BOUND");
    }

    @Test
    void sendMail_withUsedAttachment_rejectsFile() {
        SysUser alice = activeUser(1L, "alice", "Alice");
        SysUser bob = activeUser(2L, "bob", "Bob");
        FileResource file = uploadedFile("file_test_001", 1L);
        file.setMailId(99L);
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(sysUserMapper.selectActiveByUsername("bob")).thenReturn(bob);
        when(fileResourceMapper.selectByFileId("file_test_001")).thenReturn(file);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mailService.sendMail(authHeader(1L, "alice"), sendMailRequest("bob", "file_test_001"))
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertEquals("文件已被使用", exception.getMessage());
    }

    @Test
    void getMailDetail_withAttachment_returnsAttachmentObject() {
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(2L)).thenReturn(bob);
        MailDetailRow row = detailRow(100L, 1L, 2L, 1, 0);
        row.setAttachmentFileId("file_test_001");
        row.setAttachmentOriginalFilename("report.pdf");
        row.setAttachmentContentType("application/pdf");
        row.setAttachmentFileSize(2048L);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(row);

        MailDetailVO detail = mailService.getMailDetail(authHeader(2L, "bob"), 100L);

        assertNotNull(detail.getAttachment());
        assertEquals("file_test_001", detail.getAttachment().getFileId());
        assertEquals("/api/files/file_test_001/download", detail.getAttachment().getDownloadUrl());
    }

    @Test
    void getMailDetail_senderCanViewWithoutCurrentUserRecipientRecord() {
        SysUser alice = activeUser(1L, "alice", "Alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(detailRow(100L, 1L, 2L, 0, 0));

        MailDetailVO detail = mailService.getMailDetail(authHeader(1L, "alice"), 100L);

        assertEquals("SENDER", detail.getCurrentUserRole());
        assertEquals("bob", detail.getRecipient().getUsername());
        verify(mailRecipientMapper, never()).markReadIfUnread(eq(100L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    void markRead_senderCannotMarkSentMailAsRead() {
        SysUser alice = activeUser(1L, "alice", "Alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(detailRow(100L, 1L, 2L, 0, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mailService.markRead(authHeader(1L, "alice"), 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
        verify(mailRecipientMapper, never()).markReadIfUnread(eq(100L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    void deleteMail_recipientLogicalDeleteIsIdempotent() {
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(2L)).thenReturn(bob);
        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(detailRow(100L, 1L, 2L, 1, 0));

        MailDeleteResponse response = mailService.deleteMail(authHeader(2L, "bob"), 100L);

        assertEquals(100L, response.getMailId());
        assertTrue(response.getDeleted());
        assertNotNull(response.getDeletedAt());
        verify(mailRecipientMapper).deleteRecipientMailIfNotDeleted(eq(100L), eq(2L), any(LocalDateTime.class));

        when(mailMessageMapper.selectDetailByMailId(100L)).thenReturn(detailRow(100L, 1L, 2L, 1, 1));
        MailDeleteResponse secondResponse = mailService.deleteMail(authHeader(2L, "bob"), 100L);

        assertTrue(secondResponse.getDeleted());
    }

    private MailDetailRow detailRow(Long mailId, Long senderId, Long recipientId, int readFlag, int deletedFlag) {
        MailDetailRow row = new MailDetailRow();
        row.setMailId(mailId);
        row.setSubject("实验报告提交提醒");
        row.setContent("[{\"type\":\"paragraph\",\"children\":[{\"type\":\"text\",\"text\":\"请提交报告\"}]}]");
        row.setSenderId(senderId);
        row.setSenderUsername("alice");
        row.setSenderNickname("Alice");
        row.setRecipientId(recipientId);
        row.setRecipientUsername("bob");
        row.setRecipientNickname("Bob");
        row.setSentAt(LocalDateTime.of(2026, 5, 25, 16, 4));
        row.setReadFlag(readFlag);
        row.setDeletedFlag(deletedFlag);
        row.setDeletedAt(deletedFlag == 1 ? LocalDateTime.of(2026, 5, 26, 10, 30) : null);
        row.setSpamFlag(0);
        row.setAnalysisStatus("SUCCESS");
        row.setSummary("实验报告提交提醒");
        row.setSpamLevel("NONE");
        row.setSpamReason("未发现垃圾邮件特征。");
        row.setRiskLevel("SAFE");
        row.setRiskReason(null);
        row.setPriority("HIGH");
        row.setPriorityReason("包含截止日期提醒");
        row.setReplySuggestions("[\"好的，我会按时提交。\"]");
        return row;
    }

    private SendMailRequest sendMailRequest(String recipientUsername, String attachmentFileId) {
        return sendMailRequest(recipientUsername, "实验报告提交提醒", "请提交报告", attachmentFileId);
    }

    private SendMailRequest sendMailRequest(String recipientUsername,
                                            String subject,
                                            String text,
                                            String attachmentFileId) {
        SendMailRequest request = new SendMailRequest();
        request.setRecipientUsername(recipientUsername);
        request.setSubject(subject);
        request.setContent(List.of(Map.of(
                "type", "paragraph",
                "children", List.of(Map.of("type", "text", "text", text))
        )));
        request.setAttachmentFileId(attachmentFileId);
        return request;
    }

    private SendEmailRequest sendEmailRequest(String to, String subject, String text, String attachmentFileId) {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setContent(richText(text));
        request.setAttachmentFileId(attachmentFileId);
        return request;
    }

    private ReplyEmailRequest replyEmailRequest(Long mailId, Long threadId, String subject, String text) {
        ReplyEmailRequest request = new ReplyEmailRequest();
        request.setMailId(mailId);
        request.setThreadId(threadId);
        request.setSubject(subject);
        request.setContent(richText(text));
        return request;
    }

    private List<Object> richText(String text) {
        return List.of(Map.of(
                "type", "paragraph",
                "children", List.of(Map.of("type", "text", "text", text))
        ));
    }

    private SendMailCapture sendMailAndCaptureAnalysis(String subject, String text) {
        SysUser alice = activeUser(1L, "alice", "Alice");
        SysUser bob = activeUser(2L, "bob", "Bob");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(sysUserMapper.selectActiveByUsername("bob")).thenReturn(bob);
        when(mailMessageMapper.insert(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.scut.mailsystem.entity.MailMessage.class).setId(100L);
            return 1;
        });

        SendMailResponse response = mailService.sendMail(
                authHeader(1L, "alice"),
                sendMailRequest("bob", subject, text, null)
        );
        ArgumentCaptor<MailAnalysis> analysisCaptor = ArgumentCaptor.forClass(MailAnalysis.class);
        verify(mailAnalysisMapper).insert(analysisCaptor.capture());
        return new SendMailCapture(response, analysisCaptor.getValue());
    }

    private FileResource uploadedFile(String fileId, Long uploaderId) {
        FileResource file = new FileResource();
        file.setFileId(fileId);
        file.setUploaderId(uploaderId);
        file.setMailId(null);
        file.setOriginalFilename("report.pdf");
        file.setStoredFilename(fileId + ".pdf");
        file.setStoragePath("uploads/attachments/" + fileId + ".pdf");
        file.setContentType("application/pdf");
        file.setFileExt("pdf");
        file.setFileSize(2048L);
        file.setStatus("UPLOADED");
        return file;
    }

    private SysUser activeUser(Long id, String username, String nickname) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(nickname);
        return user;
    }

    private String authHeader(Long userId, String username) {
        return "Bearer " + TokenUtils.generateToken(userId, username);
    }

    private record SendMailCapture(SendMailResponse response, MailAnalysis analysis) {
    }
}
