package com.joshlong.socialhub;

import java.util.Set;

public interface UsersService {

    User userById(Integer id);

    User linkAyrshareAccounts(User user, Set<AyrshareAccount> accounts);

    Set<User> users();

    User userByName(String name);

    User newUser(String username, Set<AyrshareAccount> ayrshareAccounts);
}
