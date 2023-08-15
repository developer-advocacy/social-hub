package com.joshlong.socialhub.media;

import com.joshlong.socialhub.Media;
import com.joshlong.socialhub.MediaService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
class JdbcMediaService implements MediaService {

    private final JdbcTemplate jdbc;

    private final ApplicationEventPublisher publisher;

    JdbcMediaService(JdbcTemplate jdbc, ApplicationEventPublisher publisher) {
        this.jdbc = jdbc;
        this.publisher = publisher;
    }

    @Override
    public Media mediaByUuid(String uuid) {
        return this.jdbc.queryForObject(
                "select * from hub_media where uuid=?", new MediaRowMapper(), uuid);
    }

    @Override
    public Media newMedia(String uuid, byte[] content, MediaType contentType) {
        var sql = """
                insert into hub_media (uuid, content, content_type ) values (?,?,?) 
                on conflict on constraint hub_media_uuid_key do update set content = ?, content_type=?
                """;
        var contentTypeString = contentType.toString();
        this.jdbc.update(sql, ps -> {
            ps.setString(1, uuid);
            ps.setBytes(2, content);
            ps.setString(3, contentTypeString);
            ps.setBytes(4, content);
            ps.setString(5, contentTypeString);
        });
        var it = this.mediaByUuid(uuid);
        this.publisher.publishEvent(new MediaCreatedEvent(it));
        return it;
    }

    @Override
    public List<Media> mediaForPost(Integer postId) {
        var sql = """
                    select * from hub_media where id IN (
                        select media_fk from hub_posts_media where post_fk = ? order by ordering
                    )
                """;
        return jdbc.query(sql, new MediaRowMapper(), postId);
    }
}

