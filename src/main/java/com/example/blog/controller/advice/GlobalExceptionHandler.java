package com.example.blog.controller.advice;

import com.example.blog.exception.BusinessException;
import com.example.blog.exception.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý BusinessException (lỗi nghiệp vụ).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException businessException) {
        log.warn("BusinessException occurred: {}", businessException.getResponseStatus(), businessException);

        // Tạo phản hồi dưới dạng Map
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", businessException.getResponseStatus().getCode());
        errorResponse.put("message", businessException.getResponseStatus().getMessage());

        return ResponseEntity
                .status(businessException.getResponseStatus().getStatus())
                .body(errorResponse);
    }

    /**
     * Xử lý RuntimeException (ngoại lệ runtime không dự kiến).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException runtimeException) {
        log.error("Unexpected RuntimeException occurred: {}", runtimeException.getMessage(), runtimeException);

        // Tạo phản hồi dưới dạng Map
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "INTERNAL_SERVER_ERROR");
        errorResponse.put("message", "Unexpected error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Xử lý Exception chung (mọi lỗi khác).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception) {
        log.error("Unexpected Exception occurred: {}", exception.getMessage(), exception);

        // Tạo phản hồi dưới dạng Map
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "UNKNOWN_ERROR");
        errorResponse.put("message", "An unknown error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }


}

