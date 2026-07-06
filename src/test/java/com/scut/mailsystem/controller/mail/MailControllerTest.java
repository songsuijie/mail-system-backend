package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.service.mail.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
