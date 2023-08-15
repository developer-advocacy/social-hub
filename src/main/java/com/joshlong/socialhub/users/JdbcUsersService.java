package com.joshlong.socialhub.users;

import com.joshlong.socialhub.AyrshareAccount;
import com.joshlong.socialhub.User;
import com.joshlong.socialhub.UsersService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;


@Transactional
class JdbcUsersService implements UsersService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final Function<Integer, Collection<AyrshareAccount>> ayrshareAccountsCallback;
    private final ApplicationEventPublisher publisher;

    JdbcUsersService(JdbcTemplate jdbc, PasswordEncoder encoder,
                     Function<Integer, Collection<AyrshareAccount>> ayrshareAccountsCallback,
                     ApplicationEventPublisher publisher) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.ayrshareAccountsCallback = ayrshareAccountsCallback;
        this.publisher = publisher;
    }

    @Override
    public User userById(Integer id) {
        return this.jdbc.queryForObject(" select * from hub_users where id =?", new UserRowMapper(ayrshareAccountsCallback), id);
    }

    @Override
    public Set<User> users() {
        return Set.copyOf(this.jdbc.query(" select * from hub_users ", new UserRowMapper(ayrshareAccountsCallback)));
    }

    @Override
    public User userByName(String name) {
        return this.jdbc.queryForObject("select * from hub_users where name = ?",
                new UserRowMapper(this.ayrshareAccountsCallback), name);
    }

    @Override
    public User linkAyrshareAccounts(User user, Set<AyrshareAccount> accounts) {
        this.jdbc.update("delete from hub_users_ayrshare_accounts where users_fk = ?", user.id());
        var sql = """
                insert into hub_users_ayrshare_accounts (users_fk ,ayrshare_accounts_fk) values (?,?)
                on conflict on constraint hub_users_ayrshare_accounts_pkey do nothing  
                """;
        for (var ac : accounts) {
            this.jdbc.update(sql, user.id(), ac.id());
        }
        return this.userById(user.id());
    }

    @Override
    public User newUser(String username, Set<AyrshareAccount> ayrshareAccounts) {
        this.jdbc.update("""
                        insert into hub_users(created_date ,name)
                        values(?,?)
                        on conflict on constraint hub_users_name_key do nothing 
                        """,
                new Date(), username);
        var user = this.userByName(username);
        Assert.state(!ayrshareAccounts.isEmpty(), "you must provide a valid " + AyrshareAccount.class.getName() + " reference.");
        var returned = this.linkAyrshareAccounts(user, ayrshareAccounts);
        this.publisher.publishEvent(new UserCreatedEvent(returned));
        return returned;
    }
}
