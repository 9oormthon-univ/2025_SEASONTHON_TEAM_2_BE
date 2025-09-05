package com.seasonthon.everflow.app.global.exception.handler;

import com.seasonthon.everflow.app.global.code.BaseErrorCode;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String msg = Optional.ofNullable(fe.getDefaultMessage()).orElse("");
            errors.merge(fe.getField(), msg, (a, b) -> a + ", " + b);
        }

        ApiResponse<Map<String, String>> body =
                ApiResponse.of(ErrorStatus.VALIDATION_FAILED, errors);

        return ResponseEntity
                .status(ErrorStatus.VALIDATION_FAILED.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiResponse<Void> body =
                ApiResponse.of(ErrorStatus.MISSING_PARAMETER, null);

        return ResponseEntity
                .status(ErrorStatus.MISSING_PARAMETER.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiResponse<Void> body =
                ApiResponse.of(ErrorStatus.METHOD_NOT_ALLOWED, null);

        return ResponseEntity
                .status(ErrorStatus.METHOD_NOT_ALLOWED.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        String code = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ErrorStatus.VALIDATION_FAILED.name());

        BaseErrorCode ec = ErrorStatus.valueOf(code);
        ApiResponse<Void> body = ApiResponse.of(ec, null);

        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(GeneralException.class)
    protected ResponseEntity<ApiResponse<Void>> handleGeneral(GeneralException ex) {
        BaseErrorCode ec = ex.getCode();
        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(ApiResponse.of(ec, null));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        BaseErrorCode ec = ErrorStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(ApiResponse.of(ec, null));
    }

}
