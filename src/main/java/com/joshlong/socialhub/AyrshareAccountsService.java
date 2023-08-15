package com.joshlong.socialhub;

import java.util.Set;

public interface AyrshareAccountsService {

    AyrshareAccount ayrshareAccountById(Integer id);

    Set<AyrshareAccount> ayrshareAccountsForUser(Integer userId);

    Set<AyrshareAccount> ayrshareAccounts();

    AyrshareAccount newAyrshareAccount(String label, String bearer);
}
