package com.joshlong.socialhub.processor;

import com.joshlong.socialhub.Media;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class AyrshareHttpClient {

    private final RestTemplate restTemplate;
    private final URI socialHubApiUri;

    AyrshareHttpClient(URI host, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.socialHubApiUri = host;
    }

    private String mediaUri(Media media) {
        return this.socialHubApiUri + "/media/" + media.uuid();
    }

    String post(String message, String[] platforms, Media[] media, String ayrshareAccountBearerToken) {
        var url = "https://app.ayrshare.com/api/post";
        var uris = Stream.of(media).map(this::mediaUri).toList();

        var payload = new HashMap<String, Object>();
        payload.put("post", message);
        payload.put("platforms", platforms);

        if (!uris.isEmpty()) {
            payload.put("mediaUrls", uris);
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ayrshareAccountBearerToken);

        var request = new HttpEntity<Map<String, Object>>(payload, headers);
        return this.restTemplate.postForEntity(url, request, String.class).getBody();
    }
}

