package com.joshlong.socialhub;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record Post(User user, String text, Integer id, Instant date, Instant scheduledDate, Instant posted,
                   Media[] media, Map<AyrshareAccount, Set<String>> targets) {
}
