package com.scut.mailsystem.exception;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.common.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, getBindingErrorMessage(exception.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException exception) {
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, getBindingErrorMessage(exception.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .filter(StringUtils::hasText)
                .orElse(ErrorCode.PARAM_ERROR.getMessage());
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, "缺少必要请求参数：" + exception.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ApiResponse<Void> handleMissingServletRequestPartException(MissingServletRequestPartException exception) {
        if ("file".equals(exception.getRequestPartName())) {
            return ApiResponse.fail(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, "缺少必要请求部分：" + exception.getRequestPartName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, "请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.fail(ErrorCode.SYSTEM_ERROR);
    }

    private String getBindingErrorMessage(BindingResult bindingResult) {
        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError != null && StringUtils.hasText(fieldError.getDefaultMessage())) {
            return fieldError.getDefaultMessage();
        }
        return ErrorCode.PARAM_ERROR.getMessage();
    }
}
