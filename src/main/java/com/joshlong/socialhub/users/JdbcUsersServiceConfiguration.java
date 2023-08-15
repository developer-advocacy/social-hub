package com.joshlong.socialhub.users;

import com.joshlong.socialhub.AyrshareAccountsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class JdbcUsersServiceConfiguration {

    @Bean
    JdbcUsersService jdbcUsersService(ApplicationEventPublisher publisher, AyrshareAccountsService accountsService, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        return new JdbcUsersService(jdbcTemplate, passwordEncoder, accountsService::ayrshareAccountsForUser, publisher);
    }
}
