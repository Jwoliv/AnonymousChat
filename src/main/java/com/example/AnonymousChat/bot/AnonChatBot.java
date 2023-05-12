package com.example.AnonymousChat.bot;

import com.example.AnonymousChat.model.Session;
import com.example.AnonymousChat.model.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.MessageSender;
import com.example.AnonymousChat.util.UserComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

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
    private final Set<User> busyUsers = new CopyOnWriteArraySet<>();
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();
    //endregion collections

    //region services
    private final UserService userService;
    private MessageSender msgSender;
    //endregion services

    public AnonChatBot(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.hasMessage() && update.getMessage().hasText()) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();
            var text = msg.getText();
            var currentUser = userService.findByChatId(chatId);

            if (text.equals("/start")) {
                processingCmdStart(chatId);
            }
            else {
                if (text.equals("/new")) {
                    newConversation(chatId);
                }
                else if (text.equals("/stop")) {
                    stopConversation(chatId);
                }
                else if (text.equals("/share")) {
                    if (currentUser.getOpponentChatId() != null) {
                        var username = msg.getFrom().getUserName();
                        msgSender.sendText(currentUser.getOpponentChatId(), String.format("""
                                The user shared own username:
                                @%s it's username :)
                                Maybe you want to continue the conversation later
                                """, username));
                    }
                }
                else if (currentUser != null) {
                    var opponentChatId = chatId; //TODO: Change chatID of the opponent `currentUser.getOpponentChatId()`
                    if (msg.hasText()) {
                        SendMessage message = new SendMessage(String.valueOf(opponentChatId), text);
                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        } else if (update != null) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();

            var currentUser = userService.findByChatId(chatId);
            if (/*currentUser.getOpponentChatId() != null*/true) { //TODO: Delete the comment
                var opponentChatId = chatId; //TODO: Change chatID of the opponent `currentUser.getOpponentChatId()`
                if (msg.hasPhoto()) {
                    msgSender.sendPhoto(opponentChatId, msg.getPhoto().get(0).getFileId());
                }
                if (msg.hasVideo()) {
                    msgSender.sendVideo(opponentChatId, msg.getVideo().getFileId());
                }
                if (msg.hasAnimation()) {
                    msgSender.sendAnimation(opponentChatId, msg.getAnimation().getFileId());
                }
                if (msg.hasAudio()) {
                    msgSender.sendAudio(opponentChatId, msg.getAudio().getFileId());
                }
                if (msg.hasVoice()) {
                    msgSender.sendVoice(opponentChatId, msg.getVoice().getFileId());
                }
            }
        }
    }


    private synchronized void processingCmdStart(Long chatId) {
        var user = userService.findByChatId(chatId);
        if (user == null) {
            user = User.builder().chatId(chatId).reputation(0L).build();
            userService.save(user);
        }
    }

    private synchronized void newConversation(Long chatId) {
        var currentUser = userService.findByChatId(chatId);
        if (busyUsers.contains(currentUser)) {
            users.removeIf(x -> x.getChatId().equals(chatId));
            busyUsers.remove(currentUser);
        }

        if (users.size() == 0) {
            users.add(currentUser);
            msgSender.sendText(chatId, "Start looking for other user");
        } else {
            var otherUser = users.stream().findFirst().orElse(null);
            Session session = Session.builder().firstUser(currentUser).secondUser(otherUser).build();

            currentUser.setOpponentChatId(otherUser.getChatId());
            otherUser.setOpponentChatId(currentUser.getChatId());

            users.remove(otherUser);
            sessions.add(session);
            busyUsers.addAll(List.of(currentUser, otherUser));

            msgSender.sendText(currentUser.getChatId(), "Start session");
            msgSender.sendText(otherUser.getChatId(), "Start session");
        }
    }

    private synchronized void stopConversation(Long chatId) {
        var user = userService.findByChatId(chatId);
        if (user != null) {
            var sessionOfUser = sessions.stream()
                    .filter(x -> x.getFirstUser() == user || x.getSecondUser() == user)
                    .findFirst()
                    .orElse(null);

            if (sessionOfUser != null) {
                sessions.remove(sessionOfUser);
                busyUsers.removeAll(List.of(sessionOfUser.getFirstUser(), sessionOfUser.getSecondUser()));

                var firstUser = sessionOfUser.getFirstUser();
                var secondUser = sessionOfUser.getSecondUser();

                firstUser.setOpponentChatId(null);
                secondUser.setOpponentChatId(null);

                msgSender.sendText(sessionOfUser.getFirstUser().getChatId(), "The chat was interrupted");
                msgSender.sendText(sessionOfUser.getSecondUser().getChatId(), "The chat was interrupted");
            }
        }
    }


    private synchronized Long findOpponentChatId(Long chatId) {
        return sessions.stream()
                .filter(x -> x.getSecondUser().getChatId().equals(chatId) || x.getFirstUser().getChatId().equals(chatId))
                .findFirst()
                .map(session ->
                        session.getFirstUser().getChatId().equals(chatId)
                        ? session.getSecondUser().getChatId()
                        : session.getFirstUser().getChatId()
                ).orElse(null);
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
