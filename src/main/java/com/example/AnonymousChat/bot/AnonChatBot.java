package com.example.AnonymousChat.bot;

import com.example.AnonymousChat.handlers.ChatHandler;
import com.example.AnonymousChat.handlers.CommandHandler;
import com.example.AnonymousChat.model.user.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.message.MessageSender;
import com.example.AnonymousChat.util.UserComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


@Component
public class AnonChatBot extends TelegramLongPollingBot {

    //region config values
    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;
    //endregion system values

    //region collections
    private final Set<User> users = new ConcurrentSkipListSet<>(new UserComparator());
    //endregion collections

    //region services
    private final UserService userService;
    private MessageSender msgSender;
    //endregion services

    ///region handlers
    private final CommandHandler cmdHandler;
    private final ChatHandler chatHandler;
    ///endregion handlers

    public AnonChatBot(CommandHandler cmdHandler, ChatHandler chatHandler, UserService userService) {
        this.cmdHandler = cmdHandler;
        this.chatHandler = chatHandler;
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.hasMessage()) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();
            var currentUser = userService.findByChatId(chatId);
            if (msg.hasText()) {
                var text = msg.getText();

                if (currentUser != null && currentUser.getPreviousChatId() != null) {
                    chatHandler.changeReputationOrBlock(chatId, text, currentUser);
                }

                switch (text) {
                    case "/start" -> cmdHandler.processingCmdStart(chatId);
                    case "/new" -> chatHandler.newConversation(chatId, users);
                    case "/stop" -> chatHandler.stopConversation(chatId, users);
                    case "/share" -> chatHandler.processingCmdShare(currentUser, msg);
                    case "/info" -> cmdHandler.processingCmdInfo(currentUser);

                    default -> chatHandler.sendTextBetweenUsers(currentUser, msg);
                }
            }
            else if (currentUser != null && currentUser.getOpponentChatId() != null) {
                msgSender.sendBetweenUsers(currentUser, msg);
            }
        }
    }


    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
