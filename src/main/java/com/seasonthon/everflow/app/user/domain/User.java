package com.seasonthon.everflow.app.user.domain;

import com.seasonthon.everflow.app.family.domain.Family;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "password")
    private String password;

    @Column(name = "profile_url")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType; // ROLE_GUEST, ROLE_USER, ROLE_ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType; // KAKAO, GOOGLE, APPLE

    @Enumerated(EnumType.STRING)
    @Column(name = "status_type")
    private StatusType statusType; // ACTIVE, BANNED, WITHDRAWN

    @Enumerated(EnumType.STRING)
    @Column(name = "shelf_color", nullable = false)
    private BookshelfColor shelfColor;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "family_join_attempts")
    private Integer familyJoinAttempts;

    @Builder
    public User(
            String oauthId,
            String email,
            String nickname,
            String profileUrl,
            RoleType roleType,
            SocialType socialType,
            LocalDateTime lastLoginAt
    ) {
        this.oauthId = oauthId;
        this.email = email;
        this.nickname = nickname;
        this.password = UUID.randomUUID().toString();
        this.profileUrl = profileUrl;
        this.roleType = roleType;
        this.socialType = socialType;
        this.statusType = StatusType.ACTIVE;
        this.lastLoginAt = lastLoginAt;
        this.familyJoinAttempts = 0; // 초기 실패 횟수 0
        // 기본은 아무 제약 없이 랜덤
        this.shelfColor = BookshelfColor.random();
    }

    public boolean isWithdrawn() {
        return this.statusType == StatusType.WITHDRAWN;
    }

    public void updateRole(RoleType roleType) {
        if (roleType != null) this.roleType = roleType;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void markLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 가족 설정 시, 이미 가족 내에서 쓰고 있는 색을 최대한 피해서 색을 재배정
     * (현재 색상이 겹치지 않으면 그대로 유지)
     */
    public void setFamily(Family family) {
        this.family = family;
        if (family != null) {
            Set<BookshelfColor> usedColors = new HashSet<>();
            if (family.getMembers() != null) {
                for (User member : family.getMembers()) {
                    if (member != null && member.getShelfColor() != null) {
                        usedColors.add(member.getShelfColor());
                    }
                }
            }
            // 내 색이 비어있거나 가족 내에서 이미 사용 중이면, 겹치지 않게 다시 랜덤
            if (this.shelfColor == null || usedColors.contains(this.shelfColor)) {
                this.shelfColor = BookshelfColor.randomExcluding(usedColors);
            }
        }
    }

    public void resetFamilyJoinAttempts() {
        this.familyJoinAttempts = 0;
    }

    public void increaseFamilyJoinAttempts() {
        this.familyJoinAttempts++;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}