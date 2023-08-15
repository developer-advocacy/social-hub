package com.joshlong.socialhub.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class PostRequestJsonEncoderTest {

    private final PostJsonDecoder jsonParser = new PostJsonDecoder(new ObjectMapper());

    private final String[] platforms = "twitter,instagram".split(",");

    private final String helloWorldFileContents, fileName, base64FileContents, json;

    PostRequestJsonEncoderTest() {
        this.helloWorldFileContents = "Hello, world!";
        this.fileName = "/home/jlong/image.jpg";
        this.base64FileContents = Base64.getEncoder().encodeToString(this.helloWorldFileContents
                .getBytes(StandardCharsets.UTF_8));
        this.json = """
                { 
                    "content"   : "%s",
                    "platforms" : ["%s","%s"],
                    "media"     : [ { "name": "%s", "content" : "%s"} ] 
                }
                """.formatted(
                this.helloWorldFileContents, this.platforms[0], this.platforms[1],
                this.fileName, this.base64FileContents);
    }


    @Test
    void decode() throws Exception {
        var postRequest = this.jsonParser.decode(this.json);
        if (!postRequest.media().isEmpty()) {
            var resource = postRequest.media().values().iterator().next();
            var stringFileContents = FileCopyUtils.copyToByteArray(resource.getInputStream());
            Assertions.assertEquals(postRequest.content(), this.helloWorldFileContents);
            Assertions.assertEquals(Base64.getEncoder().encodeToString(resource.getContentAsByteArray()),
                    this.base64FileContents);
            Assertions.assertEquals(new String(stringFileContents), this.helloWorldFileContents);
            Assertions.assertArrayEquals(postRequest.platforms(), this.platforms);
        }
    }
}
