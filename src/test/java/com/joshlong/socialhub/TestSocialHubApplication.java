package com.joshlong.socialhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({TestSpecificConfiguration.class})
public class TestSocialHubApplication {

    public static void main(String[] args) {
        SpringApplication//
                .from(SocialHubApplication::main)//
                .with(TestSocialHubApplication.class)//
                .run(args);
    }

}


