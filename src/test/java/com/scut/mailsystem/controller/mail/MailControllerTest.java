package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.MailStatisticsVO;
import com.scut.mailsystem.vo.mail.RestoreMailResponse;
import com.scut.mailsystem.vo.mail.RetryAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MailControllerTest {

    private final MailService mailService = mock(MailService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MailController(mailService))
            .build();

    @Test
    void getSent_passesFinalContractFiltersToService() throws Exception {
        when(mailService.getSent(
                "Bearer token",
                2,
                20,
                "report",
                "bob",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        )).thenReturn(PageResult.of(2, 20, 0L, List.of()));

        mockMvc.perform(get("/api/mails/sent")
                        .header("Authorization", "Bearer token")
                        .param("page", "2")
                        .param("size", "20")
                        .param("keyword", "report")
                        .param("recipientUsername", "bob")
                        .param("startTime", "2026-05-01T00:00:00")
                        .param("endTime", "2026-05-31T23:59:59"))
                .andExpect(status().isOk());

        verify(mailService).getSent(
                "Bearer token",
                2,
                20,
                "report",
                "bob",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        );
    }

    @Test
    void getSpam_passesFinalContractFiltersToService() throws Exception {
        when(mailService.getSpam(
                "Bearer token",
                1,
                10,
                "gift",
                "HIGH",
                "MEDIUM",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        )).thenReturn(PageResult.of(1, 10, 0L, List.of()));

        mockMvc.perform(get("/api/mails/spam")
                        .header("Authorization", "Bearer token")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "gift")
                        .param("spamLevel", "HIGH")
                        .param("riskLevel", "MEDIUM")
                        .param("startTime", "2026-05-01T00:00:00")
                        .param("endTime", "2026-05-31T23:59:59"))
                .andExpect(status().isOk());

        verify(mailService).getSpam(
                "Bearer token",
                1,
                10,
                "gift",
                "HIGH",
                "MEDIUM",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        );
    }

    @Test
    void getTrash_passesFinalContractFiltersToService() throws Exception {
        when(mailService.getTrash(
                "Bearer token",
                1,
                10,
                "report",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        )).thenReturn(PageResult.of(1, 10, 0L, List.of()));

        mockMvc.perform(get("/api/mails/trash")
                        .header("Authorization", "Bearer token")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "report")
                        .param("startTime", "2026-05-01T00:00:00")
                        .param("endTime", "2026-05-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));

        verify(mailService).getTrash(
                "Bearer token",
                1,
                10,
                "report",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        );
    }

    @Test
    void getStatistics_returnsMailboxCounts() throws Exception {
        MailStatisticsVO statistics = new MailStatisticsVO();
        statistics.setInboxTotal(4);
        statistics.setInboxUnread(2);
        statistics.setSentTotal(3);
        statistics.setTrashTotal(1);
        statistics.setSpamTotal(2);
        when(mailService.getStatistics("Bearer token")).thenReturn(statistics);

        mockMvc.perform(get("/api/mails/statistics")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.inboxTotal").value(4))
                .andExpect(jsonPath("$.data.inboxUnread").value(2))
                .andExpect(jsonPath("$.data.sentTotal").value(3))
                .andExpect(jsonPath("$.data.trashTotal").value(1))
                .andExpect(jsonPath("$.data.spamTotal").value(2));

        verify(mailService).getStatistics("Bearer token");
    }

    @Test
    void markRead_acceptsRequiredReadBodyAndReturnsReadData() throws Exception {
        when(mailService.markRead("Bearer token", 1001L))
                .thenReturn(new MailReadResponse(1001L, true));

        mockMvc.perform(patch("/api/mails/1001/read")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "read": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1001))
                .andExpect(jsonPath("$.data.read").value(true));

        verify(mailService).markRead("Bearer token", 1001L);
    }

    @Test
    void deleteMail_returnsLogicalDeleteData() throws Exception {
        when(mailService.deleteMail("Bearer token", 1001L))
                .thenReturn(new MailDeleteResponse(
                        1001L,
                        true,
                        LocalDateTime.of(2026, 5, 26, 10, 30)
                ));

        mockMvc.perform(delete("/api/mails/1001")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1001))
                .andExpect(jsonPath("$.data.deleted").value(true));

        verify(mailService).deleteMail("Bearer token", 1001L);
    }

    @Test
    void restoreMail_returnsRestoredData() throws Exception {
        when(mailService.restoreMail("Bearer token", 1001L))
                .thenReturn(new RestoreMailResponse(1001L, false));

        mockMvc.perform(patch("/api/mails/1001/restore")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1001))
                .andExpect(jsonPath("$.data.deleted").value(false));

        verify(mailService).restoreMail("Bearer token", 1001L);
    }

    @Test
    void retryAnalysis_returnsAnalysisStatus() throws Exception {
        when(mailService.retryAnalysis("Bearer token", 1001L))
                .thenReturn(new RetryAnalysisResponse(1001L, "SUCCESS"));

        mockMvc.perform(post("/api/mails/1001/analysis/retry")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mailId").value(1001))
                .andExpect(jsonPath("$.data.analysisStatus").value("SUCCESS"));

        verify(mailService).retryAnalysis("Bearer token", 1001L);
    }
}
