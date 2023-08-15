package com.joshlong.socialhub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
        SocialHubApplication.class,
        TestSpecificConfiguration.class
})
public class PostRequestServiceTest {

    private final PostService posts;

    PostRequestServiceTest(@Autowired PostService posts) {
        this.posts = posts;
    }

    @Test
    void post() {
        var all = this.posts.posts();
        Assertions.assertEquals(all.size(), 1);
        var first = all.iterator().next();
        Assertions.assertTrue(this.posts.posted().isEmpty());
        Assertions.assertEquals(1, this.posts.notPosted().size());
        this.posts.post(first);
        Assertions.assertFalse(this.posts.posted().isEmpty());
        Assertions.assertTrue(this.posts.notPosted().isEmpty());
        this.posts.posts().forEach(System.out::println);
    }

}
