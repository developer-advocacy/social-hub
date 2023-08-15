package com.joshlong.socialhub.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

public abstract class SqlUtils {

    public static Instant instantForSqlDate(ResultSet resultSet, String columnName) throws SQLException {
        return new Date(resultSet.getDate(columnName).getTime()).toInstant();
    }
}
