package com.joshlong.socialhub;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;


@ConfigurationProperties(prefix = "socialhub")
public record HubProperties(
        Ayrshare[] ayrshares,
        URI uri,
        User[] users,
        Encryption encryption) {

    public record Ayrshare(String label, String token) {
    }

    public record User(String username, String[] accounts) {
    }

    public record Encryption(String password, String salt) {
    }
}

