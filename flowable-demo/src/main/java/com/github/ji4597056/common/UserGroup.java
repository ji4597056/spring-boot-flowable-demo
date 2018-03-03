package com.github.ji4597056.common;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * user group
 *
 * @author Jeffrey
 * @since 2018/03/01 21:45
 */
public class UserGroup {

    /**
     * key:group id, value:users
     */
    private static final Map<String, Set<User>> GROUP_STORE = new ConcurrentHashMap<>();

    public static void createGroup(String groupId) {
        GROUP_STORE.putIfAbsent(groupId, new HashSet<>());
    }

    public static void addUser(String groupId, User user) {
        addUsers(groupId, Sets.newHashSet(user));
    }

    public static void addUsers(String groupId, Set<User> users) {
        Optional.ofNullable(GROUP_STORE.get(groupId))
            .ifPresent(orignUsers -> orignUsers.addAll(users));
    }

    public static Set<User> query(String groupId) {
        return GROUP_STORE.getOrDefault(groupId, Collections.emptySet());
    }
}
