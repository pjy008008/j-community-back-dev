package com.pjy008008.j_community.service;

import com.pjy008008.j_community.entity.User;
import com.pjy008008.j_community.model.Role;
import com.pjy008008.j_community.repository.UserRepository;
import com.pjy008008.j_community.security.oauth.GithubUserInfo;
import com.pjy008008.j_community.security.oauth.GoogleUserInfo;
import com.pjy008008.j_community.security.oauth.OAuth2UserInfo;
import com.pjy008008.j_community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo;
        if (registrationId.equals("google")) {
            userInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("github")) {
            userInfo = new GithubUserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        String provider = userInfo.getProvider();
        String providerId = userInfo.getProviderId();
        String email = userInfo.getEmail();
        String username = userInfo.getName() + "_" + provider.substring(0, 2);

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(username)
                    .email(email)
                    .password(UUID.randomUUID().toString())
                    .role(Role.ROLE_USER)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);
        } else {
            // 기존 가입자 -> 정보 업데이트가 필요하면 여기서 수행
        }
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}