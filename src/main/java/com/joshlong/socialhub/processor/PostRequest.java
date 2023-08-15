package com.joshlong.socialhub.processor;

import org.springframework.core.io.Resource;

import java.util.Map;


public record PostRequest(
        String[] platforms,
        String content,
        Map<String, Resource> media) {
}
