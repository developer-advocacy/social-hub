package com.joshlong.socialhub.processor;

import com.joshlong.socialhub.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class AyrshareHttpClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
        Assert.notNull(message, "the message must not be null");
        Assert.state(platforms != null && platforms.length > 0, "you must specify a platform to which you want to publish this");
        Assert.notNull(ayrshareAccountBearerToken, "you must specify a valid Ayrshare token");

        var url = "https://app.ayrshare.com/api/post";

        var payload = new HashMap<String, Object>();
        payload.put("post", message);
        payload.put("platforms", platforms);

        if (null != media && media.length > 0) {
            log.info("we have " + media.length + " resources to publish.");
            var uris = Stream.of(media).map(this::mediaUri).toList();
            if (!uris.isEmpty()) {
                payload.put("mediaUrls", uris);
            }
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ayrshareAccountBearerToken);

        log.info("sending the following to Ayrshare with access token [" + ayrshareAccountBearerToken + "]");
        payload.forEach((key, value) -> log.info(key + '=' + value));

        var request = new HttpEntity<Map<String, Object>>(payload, headers);
        var body = this.restTemplate.postForEntity(url, request, String.class).getBody();
        log.info("tried to send a request and got the following reply [" + body + "]");
        return body;
    }
}

