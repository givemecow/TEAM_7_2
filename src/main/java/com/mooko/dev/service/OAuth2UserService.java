package com.mooko.dev.service;
import com.mooko.dev.domain.User;
import com.mooko.dev.oauth.CustomOAuth2user;
import com.mooko.dev.oauth.KakaoUserInfo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {


    @Value("${cloud.aws.s3.default-img}")
    private String USER_DEFAULT_PROFILE_IMAGE;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);

        checkUser(kakaoUserInfo);

        return new CustomOAuth2user(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName,
                kakaoUserInfo.getId()
        );
    }

    private void checkUser(KakaoUserInfo kakaoUserInfo) {


        /**
         * nickname
         * birth
         * gender
         * 는 추가 정보 입력할떄
         *
         * refreshToken은 로그인 성공하고나서 입력받기
         */
    }
}

