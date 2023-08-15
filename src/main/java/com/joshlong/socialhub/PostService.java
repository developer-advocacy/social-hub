package com.joshlong.socialhub;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface PostService {

    Post post(Post p);

    Collection<Post> posts();

    Collection<Post> posted();

    Collection<Post> notPosted();

    Post postById(Integer id);

    Post newPost(User user,
                 Media[] media,
                 Map<AyrshareAccount, Set<String>> targetPlatforms,
                 String text,
                 Instant scheduledDate);


}
