package com.joshlong.socialhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.HashSet;

@EnableConfigurationProperties(HubProperties.class)
@EnableTransactionManagement
@SpringBootApplication
public class SocialHubApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(SocialHubApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    TextEncryptor textEncryptor(HubProperties properties) {
        return Encryptors.delux(properties.encryption().password(), properties.encryption().salt());
    }

    @Bean
    ApplicationRunner applicationRunner(HubProperties properties) {
        return args -> {
            log.info("uri: " + properties.uri());
            log.info("salt: " + properties.encryption().salt());
            log.info("password: " + properties.encryption().password());
        };
    }

    @Bean
    ApplicationRunner registrationRunner(AyrshareAccountsService accounts, UsersService users, HubProperties properties) {
        return args -> {

            log.info(properties.toString());
            var newAccounts = new HashMap<String, AyrshareAccount>();
            var newUsers = new HashSet<User>();

            for (var ayrshareAccount : properties.ayrshareAccounts()) {
                var account = accounts.newAyrshareAccount(ayrshareAccount.label(), ayrshareAccount.token());
                newAccounts.put(ayrshareAccount.label(), account);
            }

            for (var user : properties.users()) {
                var accountsForThisUser = new HashSet<AyrshareAccount>();
                for (var an : user.accounts())
                    accountsForThisUser.add(newAccounts.get(an));
                newUsers.add(users.newUser(user.username(), accountsForThisUser));
            }

            newAccounts.forEach((username, account) -> log.info("initialized new account: " + account));
            newUsers.forEach(user -> log.info("initialized user: " + user));

        };
    }


}


