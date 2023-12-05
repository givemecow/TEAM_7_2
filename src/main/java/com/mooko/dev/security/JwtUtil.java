package com.mooko.dev.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.mooko.dev.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "Bearer ";
    private static final String SOCIAL_ID_CLAIM = "socialId";

    private final UserRepository userRepository;

    public String createAccessToken(String socialId) {
        Date nowDate = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(nowDate.getTime() + accessTokenExpirationPeriod))
                .withClaim(SOCIAL_ID_CLAIM, socialId)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        Date nowDate = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(nowDate.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
        log.info("Access Token 발급완료");
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }


    public String extractSocialId(String accessToken) {
        try {
            return JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(SOCIAL_ID_CLAIM)
                    .asString();
        } catch (TokenExpiredException e) {
            log.info("엑세스 토큰이 만료되었습니다.");
            throw new TokenExpiredException("엑세스 토큰이 만료되었습니다.", Instant.MAX);
        } catch (SignatureVerificationException e) {
            log.info("엑세스 토큰 서명이 유효하지 않습니다.");
            throw new SignatureVerificationException(Algorithm.HMAC512(secretKey));
        }
    }


    @Transactional
    public void updateRefreshToken(User user, String refreshToken) {
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }


    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (SignatureVerificationException e) {
            log.info("엑세스 토큰 서명이 유효하지 않습니다.");
            throw new SignatureVerificationException(Algorithm.HMAC512(secretKey));
        } catch (TokenExpiredException e){
            log.info("토큰 유효 시간이 지났습니다.");
            throw new TokenExpiredException(e.getMessage(), Instant.MAX);
        }
    }
}