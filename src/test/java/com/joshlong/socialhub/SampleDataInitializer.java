package com.joshlong.socialhub;

import org.junit.jupiter.api.Assertions;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


class SampleDataInitializer implements ApplicationRunner {

    private final Resource sample = new ClassPathResource("/sample.png");
    private final AyrshareAccountsService ayrshareAccounts;
    private final UsersService users;
    private final MediaService media;
    private final PostService posts;

    private final String joshUsername = "socialhub-joshlong";

    SampleDataInitializer(AyrshareAccountsService ayrshareAccounts, UsersService users, MediaService media, PostService posts) {
        this.ayrshareAccounts = ayrshareAccounts;
        this.users = users;
        this.media = media;
        this.posts = posts;
    }

    static void validateUser(User user) {
        Assertions.assertNotNull(user.id());
        Assertions.assertNotNull(user.created());
        Assertions.assertNotNull(user.name());
        Assertions.assertNotNull(user.ayrshareAccounts());
        Assertions.assertFalse(user.ayrshareAccounts().isEmpty());
    }

    static void mediaEquality(Media a, Media b) {
        Assertions.assertEquals(a.uuid(), b.uuid());
        Assertions.assertEquals(a.id(), b.id());
        Assertions.assertEquals(a.contentType(), b.contentType());
        Assertions.assertArrayEquals(a.content(), b.content());
    }

    Set<AyrshareAccount> accounts() {
        var joshlong = this.ayrshareAccounts.newAyrshareAccount("joshlong",
                System.getenv("AYRSHARE_JOSHLONG_BEARER_TOKEN"));
        var springcentral = this.ayrshareAccounts.newAyrshareAccount("springcentral", "5678");
        Assertions.assertNotNull(joshlong);
        Assertions.assertNotNull(springcentral);
        Assertions.assertEquals(springcentral.label(), "springcentral");
        Assertions.assertEquals(joshlong.label(), "joshlong");
        return this.ayrshareAccounts.ayrshareAccounts();
    }

    Set<User> users() {
        var accounts = this.accounts();

        this.users.newUser(joshUsername,
                accounts
                        .stream()
                        .filter(aa -> aa.label().equals("joshlong"))
                        .collect(Collectors.toSet())
        );

        var rwinch = "rwinch";
        this.users.newUser(rwinch, accounts);
        var all = this.users.users();
        Assertions.assertTrue(all.size() >= 2);
        Assertions.assertEquals(this.users.userByName(joshUsername).ayrshareAccounts().size(), 1);
        Assertions.assertEquals(this.users.userByName(rwinch).ayrshareAccounts().size(), 2);
        all.forEach(SampleDataInitializer::validateUser);
        return all;
    }

    Media[] media() throws Exception {
        var sampleContentAsByteArray = this.sample.getContentAsByteArray();
        var newMedia1 = this.media.newMedia(UUID.randomUUID().toString(), sampleContentAsByteArray, MediaType.IMAGE_PNG);
        var newMedia2 = this.media.newMedia(UUID.randomUUID().toString(), sampleContentAsByteArray, MediaType.IMAGE_PNG);
        Assertions.assertNotNull(newMedia1, "you must define some new media");
        Assertions.assertNotNull(newMedia2, "you must define some new media");
        return new Media[]{newMedia1, newMedia2};
    }

    Post post(User user) throws Exception {
        var media = this.media();
        var accounts = this.ayrshareAccounts.ayrshareAccountsForUser(user.id());
        Assertions.assertFalse(accounts.isEmpty());

        //todo do post targets platforms
        var targets = new HashMap<AyrshareAccount, Set<String>>();
        for (var ayrshareAccount : accounts)
            targets.put(ayrshareAccount, Set.of("twitter"));
        //todo do post targets platforms

        var newPost = this.posts.newPost(
                user,
                media,
                targets,
                "Hello, world, live from New York! It's @ " + Instant.now(),
                null);
        Assertions.assertNotNull(newPost);
        Assertions.assertNotNull(newPost.id());
        Assertions.assertNotNull(newPost.date());
        Assertions.assertEquals(newPost.media().length, 2);
        for (var i = 0; i < media.length; i++) {
            mediaEquality(media[i], newPost.media()[i]);
        }
        Assertions.assertNull(newPost.scheduledDate());
        Assertions.assertTrue(StringUtils.hasText(newPost.text()));
        Assertions.assertNotNull(newPost.user());
        Assertions.assertEquals(newPost.user().id(), user.id());
        Assertions.assertEquals(newPost.user(), user);

        // todo targets
        Assertions.assertEquals(newPost.targets().size(), targets.size());
        Assertions.assertFalse(newPost.targets().values().iterator().next().isEmpty());
        return newPost;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.users();
        this.post(this.users.userByName(this.joshUsername));
    }
}
