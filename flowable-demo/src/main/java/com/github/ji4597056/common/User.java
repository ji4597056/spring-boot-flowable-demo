package com.github.ji4597056.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * user entity
 *
 * @author Jeffrey
 * @since 2018/03/01 21:46
 */
public class User {

    private static final Map<String, User> USER_STORE = new ConcurrentHashMap<>();

    private final String id;

    private final String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static void createUser(String id, String name) {
        User user = new User(id, name);
        USER_STORE.putIfAbsent(id, user);
    }

    public static User getUser(String id) {
        return USER_STORE.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) {
            return false;
        }
        return name != null ? name.equals(user.name) : user.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
