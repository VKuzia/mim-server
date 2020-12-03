package com.mimteam.mimserver.security;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
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
                .map(userService::getUserByToken)
                .flatMap(this::getAuthorizedUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with token " + tokenObject));
    }

    private Optional<User> getAuthorizedUser(Optional<UserEntity> userEntity) {
        if (userEntity.isEmpty()) {
            return Optional.empty();
        }
        UserEntity user = userEntity.get();

        User authorizedUser = new User(user.getLogin(), user.getPassword(),
                true, true, true, true,
                AuthorityUtils.createAuthorityList("USER"));
        return Optional.of(authorizedUser);
    }
}
