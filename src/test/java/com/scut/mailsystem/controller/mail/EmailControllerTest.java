package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.SendEmailData;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailControllerTest {

    private final MailService mailService = mock(MailService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new EmailController(mailService))
            .build();

    @Test
    void sendEmail_acceptsRichTextContentAndAttachmentFileId() throws Exception {
        when(mailService.sendEmail(eq("Bearer token"), any(SendEmailRequest.class)))
                .thenReturn(new SendEmailData(1001L, 2001L));

        mockMvc.perform(post("/api/emails/send")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "lisi",
                                  "subject": "实验报告提交提醒",
                                  "content": [
                                    {
                                      "type": "paragraph",
                                      "children": [
                                        {
                                          "text": "请在本周五前提交实验报告"
                                        }
                                      ]
                                    }
                                  ],
                                  "attachmentFileId": "file_20260603_001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1001))
                .andExpect(jsonPath("$.data.threadId").value(2001));

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(mailService).sendEmail(eq("Bearer token"), captor.capture());
        assertEquals("lisi", captor.getValue().getTo());
        assertEquals("实验报告提交提醒", captor.getValue().getSubject());
        assertEquals("file_20260603_001", captor.getValue().getAttachmentFileId());
        assertNotNull(captor.getValue().getContent());
        assertEquals(1, captor.getValue().getContent().size());
    }

    @Test
    void replyEmail_acceptsThreadAndOriginalMailIds() throws Exception {
        when(mailService.replyEmail(eq("Bearer token"), any(ReplyEmailRequest.class)))
                .thenReturn(new SendEmailData(1002L, 2001L));

        mockMvc.perform(post("/api/emails/reply")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mailId": 1001,
                                  "threadId": 2001,
                                  "subject": "Re: 实验报告提交提醒",
                                  "content": [
                                    {
                                      "type": "paragraph",
                                      "children": [
                                        {
                                          "text": "收到，我会按时提交"
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1002))
                .andExpect(jsonPath("$.data.threadId").value(2001));

        ArgumentCaptor<ReplyEmailRequest> captor = ArgumentCaptor.forClass(ReplyEmailRequest.class);
        verify(mailService).replyEmail(eq("Bearer token"), captor.capture());
        assertEquals(1001L, captor.getValue().getMailId());
        assertEquals(2001L, captor.getValue().getThreadId());
        assertEquals("Re: 实验报告提交提醒", captor.getValue().getSubject());
        assertNotNull(captor.getValue().getContent());
        assertEquals(1, captor.getValue().getContent().size());
    }
}
