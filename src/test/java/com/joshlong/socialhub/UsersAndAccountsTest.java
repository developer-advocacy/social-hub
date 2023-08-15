package com.joshlong.socialhub;

import com.joshlong.socialhub.aryshare.AyrshareAccountCreatedEvent;
import com.joshlong.socialhub.users.UserCreatedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest(classes = {SocialHubApplication.class, TestSpecificConfiguration.class, UsersAndAccountsTest.Listener.class})
class UsersAndAccountsTest {

    private final AyrshareAccountsService ayrshareAccounts;

    UsersAndAccountsTest(@Autowired AyrshareAccountsService ayrshareAccounts) {
        this.ayrshareAccounts = ayrshareAccounts;
    }

    @Test
    void ayrshareAccounts() {
        var accounts = this.ayrshareAccounts.ayrshareAccounts();
        Assertions.assertFalse(accounts.isEmpty());
        Assertions.assertEquals(2, accounts.size());
    }

    @Test
    void users(@Autowired Listener listener) {
        Assertions.assertFalse(listener.users().isEmpty());
        Assertions.assertFalse(listener.accounts().isEmpty());
    }

    @Configuration
    static class Listener {

        private final Set<UserCreatedEvent> userCreatedEventsSet = new HashSet<>();

        private final Set<AyrshareAccountCreatedEvent> ayrshareAccountCreatedEventsSet = new HashSet<>();

        Set<UserCreatedEvent> users() {
            return this.userCreatedEventsSet;
        }

        Set<AyrshareAccountCreatedEvent> accounts() {
            return this.ayrshareAccountCreatedEventsSet;
        }

        @EventListener
        void handle(UserCreatedEvent uce) {
            userCreatedEventsSet.add(uce);
        }

        @EventListener
        void handle(AyrshareAccountCreatedEvent createdEvent) {
            this.ayrshareAccountCreatedEventsSet.add(createdEvent);
        }
    }
}

