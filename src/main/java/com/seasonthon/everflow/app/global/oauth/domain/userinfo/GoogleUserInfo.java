package com.seasonthon.everflow.app.global.oauth.domain.userinfo;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
public class GoogleUserInfo extends UserInfo {

    public GoogleUserInfo(Map<String, Object> attributes) { super(attributes); log.info(attributes.toString()); }

    @Override
    public String getId(){ return (String) attributes.get("sub"); }

    @Override
    public String getNickname() { return (String) attributes.get("nickname"); }

    @Override
    public String getEmail(){ return (String) attributes.get("email"); }

    @Override
    public String getProfileUrl() { return (String) attributes.get("picture"); }
}
