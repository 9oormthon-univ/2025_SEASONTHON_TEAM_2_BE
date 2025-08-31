package com.seasonthon.everflow.app.global.security;

import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> refreshTokenOpt = jwtService.extractRefreshToken(request);

        if (refreshTokenOpt.isPresent() && jwtService.isTokenValid(refreshTokenOpt.get())) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshTokenOpt.get());
            return;
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) throws ServletException, IOException {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    if (user.isWithdrawn()) {
                        return;
                    }
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    // roleType 파라미터 추가
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail(), user.getId(), user.getRoleType().toString()),
                            reIssuedRefreshToken);
                });
    }

    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {

        Optional<String> accessTokenOpt = jwtService.extractAccessToken(request);

        if (accessTokenOpt.isPresent()) {
            String accessToken = accessTokenOpt.get();

            if (tokenBlacklistService.isTokenBlacklisted(accessToken)) {
                // 토큰이 블랙리스트에 있으면 401 에러 반환
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 유효하지 않습니다.");
                return;
            }

            try {
                String email = jwtService.verifyTokenAndGetEmail(accessToken);
                userRepository.findByEmail(email)
                        .ifPresent(user -> {
                            if (!user.isWithdrawn()) {
                                saveAuthentication(user);
                            }
                        });
            } catch (Exception e) {
                // 토큰 검증 실패 시 401 에러 반환
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 유효하지 않습니다.");
                return;
            }
        } else {
            // 토큰이 아예 없으면 401 에러 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 유효하지 않습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(User myUser) {
        CustomUserDetails userDetailsUser = new CustomUserDetails(
                Collections.singleton(new SimpleGrantedAuthority(myUser.getRoleType().toString())),
                myUser.getEmail(),
                myUser.getRoleType(),
                myUser.getId());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
