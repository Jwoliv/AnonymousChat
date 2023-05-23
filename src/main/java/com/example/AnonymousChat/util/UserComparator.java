package com.example.AnonymousChat.util;

import com.example.AnonymousChat.model.user.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class UserComparator implements Comparator<User> {
    @Override
    public int compare(User u1, User u2) {
        return Long.compare(u1.getReputation(), u2.getReputation());
    }
}
