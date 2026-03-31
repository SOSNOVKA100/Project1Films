package com.example.project1films.config;

import com.example.project1films.dto.response.ErrorResponse;
import com.example.project1films.entity.mongo.ErrorLog;
import com.example.project1films.exception.BusinessException;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.service.MongoLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MongoLoggingService mongoLoggingService;

    public GlobalExceptionHandler(MongoLoggingService mongoLoggingService) {
        this.mongoLoggingService = mongoLoggingService;
    }

    // method collected
    private ErrorLog collectRequestInfo(HttpServletRequest request, Exception ex,
                                        int status, String errorType) {

        String userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userId = auth.getName();
        }
    //Collected param
        Map<String, String> requestParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            requestParams.put(paramName, request.getParameter(paramName));
        }

        // collected headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!"authorization".equalsIgnoreCase(headerName) &&
                        !"cookie".equalsIgnoreCase(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
        }

        Long startTime = (Long) request.getAttribute("startTime");
        Long executionTime = startTime != null ? System.currentTimeMillis() - startTime : null;

        return ErrorLog.builder()
                .errorId(UUID.randomUUID().toString())
                .errorType(errorType)
                .status(status)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .stackTrace(getStackTraceString(ex))
                .requestParams(requestParams)
                .headers(headers)
                .clientIp(getClientIp(request))
                .userId(userId)
                .userAgent(request.getHeader("User-Agent"))
                .executionTimeMs(executionTime)
                .build();
    }

    private String getStackTraceString(Exception ex) {
        return Arrays.stream(ex.getStackTrace())
                .limit(20) // Ограничиваем количество строк
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        logger.error("Resource not found: {}", ex.getMessage());

        ErrorLog errorLog = collectRequestInfo(request, ex, 404, "RESOURCE_NOT_FOUND");
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        logger.error("Duplicate resource: {}", ex.getMessage());

        ErrorLog errorLog = collectRequestInfo(request, ex, 409, "DUPLICATE_RESOURCE");
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        logger.error("Business error: {}", ex.getMessage());

        ErrorLog errorLog = collectRequestInfo(request, ex, 400, "BUSINESS_ERROR");
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        logger.error("Data integrity violation: {}", ex.getMessage());

        String message = "Database constraint violation";
        if (ex.getMessage().contains("uk_email")) {
            message = "Email already exists";
        }

        ErrorLog errorLog = collectRequestInfo(request, ex, 409, "DATA_INTEGRITY_VIOLATION");
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorLog errorLog = collectRequestInfo(request, ex, 400, "VALIDATION_ERROR");
        // Добавляем детали валидации в лог
        errorLog.setRequestParams(Map.of(
                "validationErrors",
                validationErrors.stream()
                        .map(e -> e.getField() + ": " + e.getMessage())
                        .collect(Collectors.joining(", "))
        ));
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid input data",
                request.getRequestURI()
        );
        error.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error: ", ex);

        ErrorLog errorLog = collectRequestInfo(request, ex, 500, "UNEXPECTED_ERROR");
        mongoLoggingService.saveErrorLog(errorLog);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}