package com.example.AnonymousChat.repository;

import com.example.AnonymousChat.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User> {
    User findByChatId(Long chatId);
}
