package com.joshlong.socialhub;

import java.time.Instant;
import java.util.Set;

public record User(Integer id, String name, Instant created, Set<AyrshareAccount> ayrshareAccounts) {
}
