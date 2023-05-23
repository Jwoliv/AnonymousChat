package com.example.AnonymousChat.service;

import com.example.AnonymousChat.model.user.User;
import com.example.AnonymousChat.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, UserRepository> {

    protected UserService(UserRepository repository) {
        super(repository);
    }

    public User findByChatId(Long chatId) {
        return repository.findByChatId(chatId);
    }
}
