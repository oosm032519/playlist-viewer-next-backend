package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final String spotifyAccessToken;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String spotifyAccessToken) {
        super(username, password, authorities);
        this.spotifyAccessToken = spotifyAccessToken;
    }

}
