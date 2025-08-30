package com.seasonthon.everflow.app.global.oauth.domain;

import com.seasonthon.everflow.app.global.oauth.domain.userinfo.AppleUserInfo;
import com.seasonthon.everflow.app.global.oauth.domain.userinfo.GoogleUserInfo;
import com.seasonthon.everflow.app.global.oauth.domain.userinfo.KakaoUserInfo;
import com.seasonthon.everflow.app.global.oauth.domain.userinfo.UserInfo;
import com.seasonthon.everflow.app.user.domain.RoleType;
import com.seasonthon.everflow.app.user.domain.SocialType;
import com.seasonthon.everflow.app.user.domain.User;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class IdTokenAttributes {

    private UserInfo userInfo;
    private SocialType socialType;

    public IdTokenAttributes(Map<String, Object> attributes, SocialType socialType){
        this.socialType = socialType;
        if(socialType == SocialType.GOOGLE) this.userInfo = new GoogleUserInfo(attributes);
        if(socialType == SocialType.KAKAO) this.userInfo = new KakaoUserInfo(attributes);
        if(socialType == SocialType.APPLE) this.userInfo = new AppleUserInfo(attributes);
    }

    public User toUser() {
        return User.builder()
                .socialType(socialType)
                .roleType(RoleType.ROLE_GUEST) // 최초 가입 시 GUEST 부여
                .oauthId(userInfo.getId())
                .nickname(userInfo.getNickname())
                .profileUrl(userInfo.getProfileUrl())
                .email(userInfo.getEmail())
                .lastLoginAt(LocalDateTime.now())
                // .family(null) // Family 엔티티 추가 예정
                .build();
    }
}
