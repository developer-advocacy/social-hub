package com.joshlong.socialhub;

import java.time.Instant;

public record AyrshareAccount(Integer id, String label, String bearerToken, Instant created) {
}
