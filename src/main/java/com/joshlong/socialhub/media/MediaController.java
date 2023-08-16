package com.joshlong.socialhub.media;

import com.joshlong.socialhub.Media;
import com.joshlong.socialhub.MediaService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
class MediaController {

    private final MediaService mediaService;

    MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media/{id}")
    ResponseEntity<Resource> mediaById(@PathVariable String id) {
        var media = this.mediaService.mediaByUuid(id);
        var res = (Resource) new MediaResource(media);
        return ResponseEntity
                .ok()
                .contentType(media.contentType())
                .body(res);
    }

    private static class MediaResource extends ByteArrayResource {

        MediaResource(Media media) {
            super(media.content(), media.uuid());
        }
    }
}
