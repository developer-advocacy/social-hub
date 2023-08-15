package com.joshlong.socialhub.aryshare;

import com.joshlong.socialhub.AyrshareAccount;
import com.joshlong.socialhub.AyrshareAccountsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Set;


@Service
@Transactional
class JdbcAyrshareAccountsService implements AyrshareAccountsService {

    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher publisher;

    JdbcAyrshareAccountsService(ApplicationEventPublisher publisher, JdbcTemplate template) {
        this.jdbc = template;
        this.publisher = publisher;
    }

    @Override
    public AyrshareAccount ayrshareAccountById(Integer id) {
        return this.jdbc.queryForObject(
                "select * from hub_ayrshare_accounts where id=?", new AyrshareAccountRowMapper(), id);
    }

    @Override
    public Set<AyrshareAccount> ayrshareAccountsForUser(Integer userId) {
        return Set.copyOf(this.jdbc.query(
                """ 
                        select * from hub_ayrshare_accounts haa where haa.id in 
                        (select huaa.ayrshare_accounts_fk from hub_users_ayrshare_accounts huaa where huaa.users_fk = ? )
                        """,
                new AyrshareAccountRowMapper(), userId));
    }

    @Override
    public Set<AyrshareAccount> ayrshareAccounts() {
        return Set.copyOf(this.jdbc.query("select * from hub_ayrshare_accounts", new AyrshareAccountRowMapper()));
    }

    @Override
    public AyrshareAccount newAyrshareAccount(String label, String bearerToken) {
        Assert.hasText(label, "the label is null");
        Assert.hasText(bearerToken, "the token is null");
        var sql = """
                insert into hub_ayrshare_accounts(label, created_date, bearer_token) values (? ,? ,?) 
                on conflict on constraint hub_ayrshare_accounts_label_key 
                do update set bearer_token = ?
                """;
        this.jdbc.update(sql, label, new Date(), bearerToken, bearerToken);
        var results = this.jdbc.queryForObject("select * from hub_ayrshare_accounts where label = ?", new AyrshareAccountRowMapper(), label);
        this.publisher.publishEvent(new AyrshareAccountCreatedEvent(results));
        return results;
    }
}
