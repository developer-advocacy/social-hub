package com.joshlong.socialhub.users;

import com.joshlong.socialhub.AyrshareAccountsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class JdbcUsersServiceConfiguration {

    @Bean
    JdbcUsersService jdbcUsersService(ApplicationEventPublisher publisher, AyrshareAccountsService accountsService, JdbcTemplate jdbcTemplate) {
        return new JdbcUsersService(jdbcTemplate, accountsService::ayrshareAccountsForUser, publisher);
    }
}
