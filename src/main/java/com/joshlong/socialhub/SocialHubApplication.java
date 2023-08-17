package com.joshlong.socialhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@EnableConfigurationProperties(HubProperties.class)
@EnableTransactionManagement
@SpringBootApplication
public class SocialHubApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(SocialHubApplication.class, args);
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers("/media/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());
        return http.build();
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
    ApplicationRunner configurationDebugApplicationRunner(
            @Value("${debug:false}") boolean debug,
            AyrshareAccountsService accounts, UsersService users, HubProperties properties) {
        return args -> {

            if (!debug) {
                log.info("debug=false, returning...");
                return;
            }

            log.info("---------------------------");
            log.info("uri: " + properties.uri());
            log.info("salt: " + properties.encryption().salt());
            log.info("password: " + properties.encryption().password());
            log.info("---------------------------");
            System.getenv().forEach((k, v) -> log.info("env: " + k + '=' + v));

            log.info("---------------------------");
            log.info(properties.toString());
            for (var a : properties.ayrshares())
                log.info(a.toString());
            for (var c : properties.users())
                log.info(c.toString());
            log.info("---------------------------");

            var newAccounts = new HashMap<String, AyrshareAccount>();
            var newUsers = new HashSet<User>();

            for (var ayrshareAccount : properties.ayrshares()) {
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


