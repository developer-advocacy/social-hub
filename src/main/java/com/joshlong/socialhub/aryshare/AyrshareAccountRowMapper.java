package com.joshlong.socialhub.aryshare;

import com.joshlong.socialhub.AyrshareAccount;
import com.joshlong.socialhub.utils.SqlUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class AyrshareAccountRowMapper implements RowMapper<AyrshareAccount> {

    @Override
    public AyrshareAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AyrshareAccount(
                rs.getInt("id"),
                rs.getString("label"),
                rs.getString("bearer_token"),
                SqlUtils.instantForSqlDate(rs, "created_date"));
    }
}
