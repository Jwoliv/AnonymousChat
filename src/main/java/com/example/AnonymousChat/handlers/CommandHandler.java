package com.example.AnonymousChat.handlers;

import com.example.AnonymousChat.model.user.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.message.MessageSender;
import com.example.AnonymousChat.util.message.MessagesText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {
    private MessageSender msgSender;
    private final UserService userService;

    public CommandHandler(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }


    public synchronized void processingCmdStart(Long chatId) {
        var user = userService.findByChatId(chatId);
        if (user == null) {
            user = User.builder().chatId(chatId).reputation(0L).build();
            userService.save(user);
        }
        msgSender.sendText(chatId, MessagesText.START_MESSAGE);
    }

    public synchronized void processingCmdInfo(User user) {
        if (user == null) return;

        var chatId = user.getChatId();
        var reputation = user.getReputation();
        var info = String.format(MessagesText.INFO_MESSAGE, chatId, reputation);
        msgSender.sendText(chatId, info);
    }
}
