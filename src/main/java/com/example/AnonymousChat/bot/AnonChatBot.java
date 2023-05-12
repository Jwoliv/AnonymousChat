package com.example.AnonymousChat.bot;

import com.example.AnonymousChat.model.Session;
import com.example.AnonymousChat.model.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.UserComparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
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
    private final Set<User> busyUsers = new ConcurrentSkipListSet<>();
    private final Set<Session> sessions = new ConcurrentSkipListSet<>();
    //endregion collections

    //region services
    private final UserService userService;
    //endregion services

    public AnonChatBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.hasMessage()) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();
            var text = msg.getText();

            if (text.equals("/start")) {
                var user = userService.findByChatId(chatId);
                if (user == null) {
                    user = User.builder().chatId(chatId).reputation(0L).build();
                    userService.save(user);
                }
            }
            else {
                if (text.equals("/new")) {
                    synchronized (this) {
                        var currentUser = userService.findByChatId(chatId);
                        if (busyUsers.contains(currentUser)) {
                            users.removeIf(x -> x.getChatId().equals(chatId));
                            busyUsers.remove(currentUser);
                        }

                        if (users.size() == 0) {
                            users.add(currentUser);
                            sendMessage(chatId, "Start looking for other user");
                        } else {
                            var otherUser = users.stream().findFirst().orElse(null);
                            Session session = Session.builder().firstUser(currentUser).secondUser(otherUser).build();

                            users.remove(otherUser);
                            sessions.add(session);
                            busyUsers.addAll(List.of(currentUser, otherUser));

                            sendMessage(currentUser.getChatId(), "Start session");
                            sendMessage(otherUser.getChatId(), "Start session");
                        }
                    }
                }
                else if (text.equals("/stop")) {
                    synchronized (this) {
                        var user = userService.findByChatId(chatId);
                        if (user != null) {
                            var sessionOfUser = sessions.stream()
                                    .filter(x -> x.getFirstUser() == user || x.getSecondUser() == user)
                                    .findFirst()
                                    .orElse(null);

                            if (sessionOfUser != null) {
                                sessions.remove(sessionOfUser);
                                busyUsers.removeAll(List.of(sessionOfUser.getFirstUser(), sessionOfUser.getSecondUser()));

                                sendMessage(sessionOfUser.getFirstUser().getChatId(), "The chat was interrupted");
                                sendMessage(sessionOfUser.getSecondUser().getChatId(), "The chat was interrupted");
                            }
                        }
                    }
                }
            }
        }
    }


    public void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
