package com.joshlong.socialhub;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestSpecificConfiguration {

    @Bean
    SampleDataInitializer sampleDataInitialization(
            AyrshareAccountsService accountsService,
            PostService postService,
            MediaService mediaService,
            UsersService usersService) {
        return new SampleDataInitializer(accountsService, usersService,
                mediaService, postService);
    }
}

