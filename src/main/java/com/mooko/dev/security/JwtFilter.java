package com.mooko.dev.security;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL_LOGIN = "/oauth2/authorization/kakao";
    private static final String NO_CHECK_URL_REDIRECT = "/login/oauth2/code/kakao";
//    private static final String NO_CHECK_URL_HANDSHAKING_FOR_CHECK = "/ws-check/**";
//    private static final String NO_CHECK_URL_HANDSHAKING_BUTTON = "/ws-button/**";
//    private static final String NO_CHECK_URL_HANDSHAKING_LEAVE = "/ws-leave-event/**";
    private static final String NO_CHECK_URL_EVENT_BLOCK = "/api/v1/event/image-list/**";
    private static final String NO_CHECK_URL_EVENT_SAVE_PHOTO = "/api/v1/event/save-photo/**";


    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AntPathMatcher antPathMatcher;
    private final GrantedAuthoritiesMapper authoritiesMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isPathExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        processTokenAuthentication(request, response);
        filterChain.doFilter(request, response);
    }

    private boolean isPathExcluded(String path) {
        return antPathMatcher.match(NO_CHECK_URL_LOGIN, path)
                || antPathMatcher.match(NO_CHECK_URL_REDIRECT, path)
                || antPathMatcher.match(NO_CHECK_URL_EVENT_BLOCK, path)
                || antPathMatcher.match(NO_CHECK_URL_EVENT_SAVE_PHOTO, path);
    }

    private void processTokenAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = jwtUtil.extractRefreshToken(request)
                    .filter(jwtUtil::isTokenValid)
                    .orElse(null);

            if (refreshToken != null) {
                reIssueAccessToken(refreshToken, response);
            } else {
                authenticateWithAccessToken(request, response);
            }
        } catch (SignatureVerificationException | TokenExpiredException e) {
            log.error("토큰 검증 실패: " + e.getMessage());
            throw new BadCredentialsException("토큰 인증 실패", e);
        }
    }


    private void reIssueAccessToken(String refreshToken, HttpServletResponse response) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String newAccessToken = jwtUtil.createAccessToken(user.getSocialId());
                    jwtUtil.sendAccessToken(response, newAccessToken);
                    saveAuthentication(user);
                });
    }

    private void authenticateWithAccessToken(HttpServletRequest request, HttpServletResponse response) {
        jwtUtil.extractAccessToken(request)
                .ifPresent(accessToken -> {
                    try {
                        String socialId = jwtUtil.extractSocialId(accessToken);
                        userRepository.findBySocialId(socialId).ifPresent(this::saveAuthentication);
                    } catch (TokenExpiredException | SignatureVerificationException e) {
                        log.error("인증 실패: " + e.getMessage());
                        throw new BadCredentialsException("토큰 검증 실패", e);
                    }
                });
    }


    private void saveAuthentication(User user) {
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, authoritiesMapper.mapAuthorities(principalDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
