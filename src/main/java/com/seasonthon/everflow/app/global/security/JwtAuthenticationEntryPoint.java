package com.seasonthon.everflow.app.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 인증 / 인가에 따른 반환 상태 설정
 * - 401 Unauthorized → 미인증
 * - 403 Forbidden → 미인가
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ApiResponse<Object> UNAUTHORIZED_RESPONSE =
            ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED.getCode(), ErrorStatus.UNAUTHORIZED.getMessage(), null);

    private static final ApiResponse<Object> FORBIDDEN_RESPONSE =
            ApiResponse.onFailure(ErrorStatus.FORBIDDEN.getCode(), ErrorStatus.FORBIDDEN.getMessage(), null);

    /**
     * 인증 실패 (401) 처리
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");

        try (OutputStream os = response.getOutputStream()) {
            objectMapper.writeValue(os, UNAUTHORIZED_RESPONSE);
            os.flush();
        }
    }

    /**
     * 인가 실패 (403) 처리
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        try (OutputStream os = response.getOutputStream()) {
            objectMapper.writeValue(os, FORBIDDEN_RESPONSE);
            os.flush();
        }
    }
}
