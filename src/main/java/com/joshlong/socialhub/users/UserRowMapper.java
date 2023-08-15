package com.joshlong.socialhub.users;

import com.joshlong.socialhub.AyrshareAccount;
import com.joshlong.socialhub.User;
import com.joshlong.socialhub.utils.SqlUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class UserRowMapper implements RowMapper<User> {

    private final Map<Integer, Set<AyrshareAccount>> accountsMapping = new ConcurrentHashMap<>();
    private final Map<Integer, User> usersMapping = new ConcurrentHashMap<>();
    private final Function<Integer, Collection<AyrshareAccount>> loadAyrshareAccountInstancesCallback;

    UserRowMapper(Function<Integer, Collection<AyrshareAccount>> callback) {
        this.loadAyrshareAccountInstancesCallback = callback;
    }

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        var usersId = rs.getInt("id");
        this.accountsMapping.computeIfAbsent(usersId, u -> {
            var ayrshareAccounts = this.loadAyrshareAccountInstancesCallback.apply(u);
            return Set.copyOf(ayrshareAccounts);
        });
        var user = new User(usersId, rs.getString("name"),
                SqlUtils.instantForSqlDate(rs, "created_date"), this.accountsMapping.getOrDefault(usersId, new HashSet<>()));
        this.usersMapping.putIfAbsent(user.id(), user);
        return this.usersMapping.get(usersId);
    }
}
