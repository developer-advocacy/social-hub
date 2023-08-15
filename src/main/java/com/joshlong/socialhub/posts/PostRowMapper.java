package com.joshlong.socialhub.posts;

import com.joshlong.socialhub.AyrshareAccount;
import com.joshlong.socialhub.Media;
import com.joshlong.socialhub.Post;
import com.joshlong.socialhub.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class PostRowMapper implements RowMapper<Post> {

    private final Function<Integer, User> userByIdResolver;
    private final Function<Integer, Media[]> mediaResolver;
    private final Function<Integer, Map<AyrshareAccount, Set<String>>> targetsResolver;

    PostRowMapper(Function<Integer, User> userByIdResolver, Function<Integer, Media[]> mediaResolver, Function<Integer, Map<AyrshareAccount, Set<String>>> targetsResolver) {
        this.userByIdResolver = userByIdResolver;
        this.mediaResolver = mediaResolver;
        this.targetsResolver = targetsResolver;
    }

    private static Instant toInstant(Date date) {
        if (date == null) return null;
        return new java.util.Date((date.getTime())).toInstant();
    }

    @Override
    public Post mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        var user = this.userByIdResolver.apply(resultSet.getInt("user_fk"));
        var postId = resultSet.getInt("id");
        var media = this.mediaResolver.apply(postId);
        var targets = this.targetsResolver.apply(postId);
        return new Post(user, resultSet.getString("text"), postId, toInstant(resultSet.getDate("date")),
                toInstant(resultSet.getDate("scheduled_date")),
                toInstant(resultSet.getDate("posted")), media, targets);
    }
}

