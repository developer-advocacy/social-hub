package com.joshlong.socialhub.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Component
class PostJsonDecoder {

    private final ObjectMapper objectMapper;

    PostJsonDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    PostRequest decode(String json) {
        try {
            var root = this.objectMapper.readTree(json);
            var media = this.objectMapper.convertValue(root.get("media"),
                    new TypeReference<Map<String, String>[]>() {
                    });
            var map = new HashMap<String, Resource>();
            for (var m : media) {
                var contents = Base64.getDecoder().decode(m.get("content"));
                var mediaTypeResource = new ContentTypeResource(contents, MediaType.parseMediaType(m.get("content-type")));
                map.put(m.get("name"), mediaTypeResource);
            }
            return new PostRequest(this.objectMapper.convertValue(root.get("platforms"), String[].class),
                    root.get("content").asText(), map);
        }//
        catch (Exception iae) {
            throw new IllegalArgumentException(iae);
        }
    }

    public static class ContentTypeResource extends ByteArrayResource {

        private final MediaType mediaType;

        public ContentTypeResource(byte[] byteArray, MediaType mediaType) {
            super(byteArray);
            this.mediaType = mediaType;
        }

        public MediaType mediaType() {
            return this.mediaType;
        }
    }
}
