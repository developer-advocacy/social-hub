package com.joshlong.socialhub.processor;

import com.joshlong.socialhub.UsersService;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;

import java.util.function.Supplier;

/**
 * look for a JWT in the Spring Integration message, make sure it's valid by contacting the issuer URI,
 * and then making sure it lines up with a user in the local system.
 */
class JwtAuthenticatingAuthorizationManager implements AuthorizationManager<Message<?>> {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private final JwtDecoder decoder;

    private final UsersService users;

    JwtAuthenticatingAuthorizationManager(JwtAuthenticationConverter jwtAuthenticationConverter, JwtDecoder decoder, UsersService users) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.decoder = decoder;
        this.users = users;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, Message<?> object) {
        var token = (String) object.getHeaders().get(HttpHeaders.AUTHORIZATION);
        Assert.hasText(token, "the token must be non-empty!");
        var decodedJwt = this.decoder.decode(token);
        var authenticationToken = this.jwtAuthenticationConverter.convert(decodedJwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        var auth = authentication.get();
        if (auth.isAuthenticated()) {
            var username = auth.getName();
            var user = this.users.userByName(username);
            Assert.notNull(user, "the user must not be null");
            return new AuthorizationDecision(true);
        }
        return new AuthorizationDecision(false);
    }
}
