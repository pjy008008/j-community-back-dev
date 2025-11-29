package com.pjy008008.j_community.security.oauth;

import java.util.Map;

public class GithubUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public GithubUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() { return String.valueOf(attributes.get("id")); }
    @Override
    public String getProvider() { return "github"; }
    @Override
    public String getEmail() { return (String) attributes.get("email"); }
    @Override
    public String getName() { return (String) attributes.get("login"); }
}