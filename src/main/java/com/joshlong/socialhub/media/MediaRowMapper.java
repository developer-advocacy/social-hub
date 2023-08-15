package com.joshlong.socialhub.media;

import com.joshlong.socialhub.Media;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.MimeType;

import java.sql.ResultSet;
import java.sql.SQLException;

class MediaRowMapper implements RowMapper<Media> {

    @Override
    public Media mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Media(rs.getInt("id"),
                rs.getString("uuid"),
                rs.getBytes("content"),
                MediaType.valueOf(rs.getString("content_type"))
        );
    }
}
