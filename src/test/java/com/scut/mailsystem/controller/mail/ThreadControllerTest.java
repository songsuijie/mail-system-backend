package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.ThreadDetailVO;
import com.scut.mailsystem.vo.mail.ThreadListItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTest {

    private final MailService mailService = mock(MailService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ThreadController(mailService))
            .build();

    @Test
    void getThreads_passesFinalContractFiltersToService() throws Exception {
        when(mailService.getThreads(
                "Bearer token",
                1,
                10,
                "report",
                "unread",
                "zhangsan",
                "HIGH",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        )).thenReturn(PageResult.of(1, 10, 0L, List.<ThreadListItemVO>of()));

        mockMvc.perform(get("/api/threads")
                        .header("Authorization", "Bearer token")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "report")
                        .param("readStatus", "unread")
                        .param("senderUsername", "zhangsan")
                        .param("priority", "HIGH")
                        .param("startTime", "2026-05-01T00:00:00")
                        .param("endTime", "2026-05-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(mailService).getThreads(
                "Bearer token",
                1,
                10,
                "report",
                "unread",
                "zhangsan",
                "HIGH",
                "2026-05-01T00:00:00",
                "2026-05-31T23:59:59"
        );
    }

    @Test
    void getThreadDetail_passesCursorAndLimitToService() throws Exception {
        ThreadDetailVO detail = new ThreadDetailVO();
        detail.setThreadId(2001L);
        detail.setSubject("实验报告提交提醒");
        detail.setTotal(2);
        detail.setLimit(20);
        detail.setNextCursor(null);
        detail.setHasMore(false);
        detail.setMails(List.of());
        when(mailService.getThreadDetail("Bearer token", 2001L, "1001", 20)).thenReturn(detail);

        mockMvc.perform(get("/api/threads/2001")
                        .header("Authorization", "Bearer token")
                        .param("cursor", "1001")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.threadId").value(2001))
                .andExpect(jsonPath("$.data.subject").value("实验报告提交提醒"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.limit").value(20))
                .andExpect(jsonPath("$.data.hasMore").value(false));

        verify(mailService).getThreadDetail("Bearer token", 2001L, "1001", 20);
    }
}
