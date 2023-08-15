package com.joshlong.socialhub.posts;

import com.joshlong.socialhub.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.Date;
import java.sql.Types;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;


@Transactional
class JdbcPostService implements PostService {

    private final JdbcTemplate jdbc;
    private final RowMapper<Post> postRowMapper;
    private final Function<Integer, AyrshareAccount> ayrshareAccountResolver;
    private final RowMapper<Target> rowMapperForTargetPlatform =
            (rs, rowNum) -> new Target(rs.getInt("post_fk"), rs.getString("platform"), rs.getInt("ayrshare_account_fk"));

    JdbcPostService(JdbcTemplate jdbc,
                    Function<Integer, Media[]> mediaResolver,
                    Function<Integer, AyrshareAccount> accountResolver,
                    Function<Integer, User> userByIdResolver) {
        this.jdbc = jdbc;
        this.ayrshareAccountResolver = accountResolver;
        this.postRowMapper = new PostRowMapper(userByIdResolver, mediaResolver, this::targetsFor);
    }


    private Map<AyrshareAccount, Set<String>> targetsFor(Integer postId) {
        var accounts = new HashMap<Integer, AyrshareAccount>();
        var results = this.jdbc.query(" select * from hub_posts_target_platforms where post_fk =?",
                this.rowMapperForTargetPlatform, postId);
        var map = new HashMap<AyrshareAccount, Set<String>>();
        for (var target : results) {
            accounts.computeIfAbsent(target.ayrshareAccount(), this.ayrshareAccountResolver);
            var account = accounts.get(target.ayrshareAccount());
            map.computeIfAbsent(account, ayrshareAccount -> new HashSet<>());
            map.get(account).add(target.platform());
        }
        return map;
    }

    @Override
    public Post post(Post p) {
        this.jdbc.update("update hub_posts set posted = NOW() where id = ? ", p.id());
        return this.postById(p.id());
    }

    @Override
    public Collection<Post> posts() {
        return this.jdbc.query("select * from hub_posts", this.postRowMapper);
    }

    @Override
    public Collection<Post> posted() {
        return this.jdbc.query("select * from hub_posts where posted is not null", this.postRowMapper);
    }

    @Override
    public Collection<Post> notPosted() {
        return this.jdbc.query("select * from hub_posts where posted is null", this.postRowMapper);
    }

    @Override
    public Post postById(Integer id) {
        return this.jdbc.queryForObject(
                "  select * from hub_posts where id =? ", this.postRowMapper, id);
    }

    @Override
    public Post newPost(User user, Media[] allMedia, Map<AyrshareAccount, Set<String>> targetPlatforms, String text, Instant scheduledDate) {
        Assert.notNull(user, "the user must not be null");
        Assert.notNull(targetPlatforms, "you must provide one or more target platforms");
        Assert.isTrue(!targetPlatforms.values().isEmpty(), "you must provide one or more target platforms");
        Assert.notNull(text, "the text must not be null");
        if (allMedia == null) allMedia = new Media[0];
        var sql = """
                insert into hub_posts( text, date, scheduled_date, user_fk) values (?,?,?,?)
                """;
        var gkh = new GeneratedKeyHolder();
        this.jdbc.update(connection -> {
                    var preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                    preparedStatement.setString(1, text);
                    preparedStatement.setDate(2, new Date(Instant.now().toEpochMilli()));
                    if (scheduledDate == null) {
                        preparedStatement.setNull(3, Types.DATE);
                    }//
                    else {
                        preparedStatement.setDate(3, new Date(scheduledDate.toEpochMilli()));
                    }
                    preparedStatement.setInt(4, user.id());
                    return preparedStatement;
                }, gkh
        );
        var postId = (Integer) gkh.getKeys().get("id");

        // targets
        var targetPlatformDdl = """
                insert into hub_posts_target_platforms( ayrshare_account_fk, post_fk, platform) values (?,?,?)  
                on conflict on constraint hub_posts_target_platforms_ayrshare_account_fk_post_fk_plat_key do nothing 
                """;
        for (var account : targetPlatforms.keySet())
            for (var socialPlatform : targetPlatforms.get(account))
                this.jdbc.update(targetPlatformDdl, account.id(), postId, socialPlatform);

        // media
        var hubPostsMediaSql = """
                insert into hub_posts_media (post_fk, media_fk, ordering) values (?,?,?)
                on conflict on constraint hub_posts_media_post_fk_media_fk_key do update set ordering = ? 
                """;
        for (var ordering = 0; ordering < allMedia.length; ordering++) {
            var media = allMedia[ordering];
            var mediaId = media.id();
            this.jdbc.update(hubPostsMediaSql, postId, mediaId, ordering, ordering);
        }
        return this.postById(postId);
    }

    private record Target(Integer postId, String platform, Integer ayrshareAccount) {
    }


}

