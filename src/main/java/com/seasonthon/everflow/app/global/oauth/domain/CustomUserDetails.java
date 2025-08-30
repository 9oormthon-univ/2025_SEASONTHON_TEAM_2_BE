package com.seasonthon.everflow.app.global.oauth.domain;

import com.seasonthon.everflow.app.user.domain.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Getter;

import java.util.*;

@Getter
public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private RoleType roleType;

    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(Collection<? extends GrantedAuthority> authorities,
                             String username,
                             RoleType roleType, Long userId) {

        this.authorities = (authorities != null)
                ? Collections.unmodifiableSet(new LinkedHashSet<>(this.sortAuthorities(authorities)))
                : Collections.unmodifiableSet(new LinkedHashSet<>(AuthorityUtils.NO_AUTHORITIES));
        this.userId = userId;
        this.username = username;
        this.password = null;
        this.roleType = roleType;
    }

    private Set<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {

        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(
                Comparator.comparing(GrantedAuthority::getAuthority));
        sortedAuthorities.addAll(authorities);
        return sortedAuthorities;
    }
}
