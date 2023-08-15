package com.joshlong.socialhub.posts;

import com.joshlong.socialhub.AyrshareAccountsService;
import com.joshlong.socialhub.Media;
import com.joshlong.socialhub.MediaService;
import com.joshlong.socialhub.UsersService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class JdbcPostsServiceConfiguration {

    @Bean
    JdbcPostService jdbcPostService(AyrshareAccountsService as, JdbcTemplate template, UsersService service, MediaService mediaService) {
        return new JdbcPostService(template, postId -> mediaService.mediaForPost(postId).toArray(new Media[0]),
                as::ayrshareAccountById, service::userById);
    }
}
