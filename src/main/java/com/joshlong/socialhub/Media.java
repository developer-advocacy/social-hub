package com.joshlong.socialhub;

import org.springframework.http.MediaType;

public record Media(Integer id, String uuid, byte[] content, MediaType contentType) {
}