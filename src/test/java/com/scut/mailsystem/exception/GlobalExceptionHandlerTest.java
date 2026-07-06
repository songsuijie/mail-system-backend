package com.scut.mailsystem.exception;

import com.scut.mailsystem.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ErrorStatusController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void businessException_unauthorizedReturnsHttp401() throws Exception {
        mockMvc.perform(get("/test-errors/unauthorized").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40002));
    }

    @Test
    void businessException_forbiddenReturnsHttp403() throws Exception {
        mockMvc.perform(get("/test-errors/forbidden").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40003));
    }

    @Test
    void businessException_notFoundReturnsHttp404() throws Exception {
        mockMvc.perform(get("/test-errors/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40004));
    }

    @Test
    void businessException_conflictReturnsHttp409() throws Exception {
        mockMvc.perform(get("/test-errors/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40005));
    }

    @Test
    void businessException_paramErrorReturnsHttp400() throws Exception {
        mockMvc.perform(get("/test-errors/param-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void unexpectedExceptionReturnsHttp500() throws Exception {
        mockMvc.perform(get("/test-errors/system-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(50000));
    }

    @RestController
    static class ErrorStatusController {

        @GetMapping("/test-errors/unauthorized")
        void unauthorized() {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        @GetMapping("/test-errors/forbidden")
        void forbidden() {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        @GetMapping("/test-errors/not-found")
        void notFound() {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        @GetMapping("/test-errors/conflict")
        void conflict() {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        @GetMapping("/test-errors/param-error")
        void paramError() {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        @GetMapping("/test-errors/system-error")
        void systemError() {
            throw new IllegalStateException("boom");
        }
    }
}
