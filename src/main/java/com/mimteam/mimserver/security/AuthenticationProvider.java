package com.mimteam.mimserver.security;

import com.mimteam.mimserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private final UserService userService;

    @Autowired
    public AuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        Object tokenObject = authentication.getCredentials();
        return Optional.ofNullable(tokenObject)
                .map(String::valueOf)
                .flatMap(userService::getUserByToken)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with token " + tokenObject));
    }
}
