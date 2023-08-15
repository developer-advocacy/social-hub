package com.joshlong.socialhub;

import org.springframework.http.MediaType;

import java.util.List;

public interface MediaService {

    Media newMedia(String uuid, byte[] content, MediaType contentType);

    Media mediaByUuid(String name);

    List<Media> mediaForPost(Integer postId);
}
