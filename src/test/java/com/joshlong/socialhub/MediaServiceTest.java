package com.joshlong.socialhub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.UUID;

@SpringBootTest(classes = {SocialHubApplication.class, TestSpecificConfiguration.class})
class MediaServiceTest {

    private final MediaService media;

    private final Resource sample = new ClassPathResource("/sample.png");

    MediaServiceTest(@Autowired MediaService media) {
        this.media = media;
    }

    static void matches(Media media, String uuid, byte[] bytes, MediaType mediaType) {
        Assertions.assertArrayEquals(media.content(), bytes);
        Assertions.assertEquals(media.contentType(), mediaType);
        Assertions.assertEquals(media.uuid(), uuid);
    }

    @Test
    void media() throws Exception {
        var uuid = UUID.randomUUID().toString();
        var contentAsByteArray = this.sample.getContentAsByteArray();
        var imagePng = MediaType.IMAGE_PNG;
        matches(this.media.newMedia(uuid, contentAsByteArray, imagePng), uuid, contentAsByteArray, imagePng);
        matches(this.media.mediaByUuid(uuid), uuid, contentAsByteArray, imagePng);
    }
}