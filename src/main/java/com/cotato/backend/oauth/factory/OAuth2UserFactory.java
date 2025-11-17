package com.cotato.backend.oauth.factory;

import com.cotato.backend.domain.oauth.OAuth2Profile;
import com.cotato.backend.domain.oauth.OAuth2UserAttribute;
import com.cotato.backend.domain.oauth.Role;
import com.cotato.backend.domain.user.entity.CustomOAuth2User;
import com.cotato.backend.domain.user.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class OAuth2UserFactory {

    public OAuth2User createOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User, OAuth2Profile userProfile, User user) {
        String userNameAttributeName = getUserNameAttributeName(userRequest);
        OAuth2UserAttribute oAuth2UserAttribute = new OAuth2UserAttribute(oAuth2User.getAttributes(), userNameAttributeName, userProfile);

        DefaultOAuth2User defaultOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.getValue())),
                oAuth2UserAttribute.getAttributes(),
                userNameAttributeName
        );

        return new CustomOAuth2User(user, defaultOAuth2User);
    }

    private String getUserNameAttributeName(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
    }
}