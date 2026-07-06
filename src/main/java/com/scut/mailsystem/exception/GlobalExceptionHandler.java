package com.scut.mailsystem.exception;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.common.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        return fail(ErrorCode.PARAM_ERROR, getBindingErrorMessage(exception.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return fail(ErrorCode.PARAM_ERROR, getBindingErrorMessage(exception.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .filter(StringUtils::hasText)
                .orElse(ErrorCode.PARAM_ERROR.getMessage());
        return fail(ErrorCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return fail(ErrorCode.PARAM_ERROR, "缺少必要请求参数：" + exception.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPartException(
            MissingServletRequestPartException exception) {
        if ("file".equals(exception.getRequestPartName())) {
            return fail(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        return fail(ErrorCode.PARAM_ERROR, "缺少必要请求部分：" + exception.getRequestPartName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        return fail(ErrorCode.PARAM_ERROR, "请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return fail(ErrorCode.SYSTEM_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    private ResponseEntity<ApiResponse<Void>> fail(Integer code, String message) {
        return ResponseEntity.status(httpStatusForCode(code)).body(ApiResponse.fail(code, message));
    }

    private HttpStatus httpStatusForCode(Integer code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (code) {
            case 40000, 40007, 40008 -> HttpStatus.BAD_REQUEST;
            case 40001, 40002 -> HttpStatus.UNAUTHORIZED;
            case 40003 -> HttpStatus.FORBIDDEN;
            case 40004 -> HttpStatus.NOT_FOUND;
            case 40005, 40006 -> HttpStatus.CONFLICT;
            case 50000, 50001 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private String getBindingErrorMessage(BindingResult bindingResult) {
        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError != null && StringUtils.hasText(fieldError.getDefaultMessage())) {
            return fieldError.getDefaultMessage();
        }
        return ErrorCode.PARAM_ERROR.getMessage();
    }
}
