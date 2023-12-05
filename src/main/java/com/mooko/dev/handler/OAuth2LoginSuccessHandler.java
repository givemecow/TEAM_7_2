package com.mooko.dev.handler;


import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;

import com.mooko.dev.oauth.CustomOAuth2user;
import com.mooko.dev.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공 토큰 세팅에 들어갑니다");

        CustomOAuth2user customOAuth2user = (CustomOAuth2user) authentication.getPrincipal();
        String socialId = customOAuth2user.getId();
        User user = userRepository.findBySocialId(socialId).orElseThrow(() -> new UsernameNotFoundException("유저를 찾지 못했습니다"));
        PrincipalDetails principalDetails = new PrincipalDetails(user);


        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(principalDetails, null,
                        authoritiesMapper.mapAuthorities(principalDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        String accessToken = jwtUtil.createAccessToken(user.getSocialId());
        String refreshToken = jwtUtil.createRefreshToken();

        jwtUtil.updateRefreshToken(user, refreshToken);

        Cookie accessCookie = new Cookie("access_cookie", accessToken);
        Cookie refreshCookie = new Cookie("refresh_cookie", refreshToken);

//        accessCookie.setDomain("localhost");  // 도메인을 localhost로 설정
        accessCookie.setPath("/");
        refreshCookie.setPath("/");
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
//        response.sendRedirect("/profile");

//        response.sendRedirect("http://localhost:5173/substart");
        response.sendRedirect("https://moodbarcode.com/substart");

    }
}
