package CEOS.concurrency.common.exception;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.code.GeneralErrorCode;
import CEOS.concurrency.common.response.Response;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.core.convert.ConversionFailedException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException e) {
        BusinessErrorCode errorCode = e.getBusinessErrorCode();
        log.warn("BusinessException: {} - {}", errorCode.name(), e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    // 일반 예외
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Response<Void>> handleGeneralException(GeneralException e) {
        GeneralErrorCode errorCode = e.getErrorCode();
        log.warn("GeneralException: {} - {}", errorCode.name(), e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    // ==================== 보안 관련 예외 ====================

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class, AuthenticationException.class})
    public ResponseEntity<Response<Void>> handleSecurityException(Exception e) {
        GeneralErrorCode errorCode;
        if (e instanceof AuthenticationException) {
            errorCode = GeneralErrorCode.UNAUTHORIZED;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                errorCode = GeneralErrorCode.UNAUTHORIZED;
            } else {
                errorCode = GeneralErrorCode.FORBIDDEN;
            }
        }
        log.warn("Security Error: {} - {}", errorCode.name(), e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    // ==================== Validation 예외 ====================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(ConstraintViolationException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.VALIDATION_ERROR;
        String detailedErrors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("ConstraintViolationException - Validation errors: [{}]", detailedErrors);

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode, null, detailedErrors));
    }

    // @RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.VALIDATION_ERROR;
        String detailedErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("MethodArgumentNotValidException - Field errors: [{}]", detailedErrors);

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode, null, detailedErrors));
    }

    // ==================== HTTP 요청 예외 ====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFound(NoResourceFoundException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.NOT_FOUND;
        log.warn("NoResourceFoundException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.METHOD_NOT_ALLOWED;
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.UNSUPPORTED_MEDIA_TYPE;
        log.warn("HttpMediaTypeNotSupportedException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseEntity<Response<Void>> handleTypeMismatch(Exception e) {
        GeneralErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
        log.warn("TypeMismatchException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        GeneralErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception e) {
        GeneralErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        log.error("Unhandled Exception: ", e);

        return ResponseEntity.status(errorCode.getStatusCode()).body(Response.of(errorCode));
    }
}