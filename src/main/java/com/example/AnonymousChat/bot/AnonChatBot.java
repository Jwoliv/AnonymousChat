package com.example.AnonymousChat.bot;

import com.example.AnonymousChat.model.Session;
import com.example.AnonymousChat.model.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.MessageSender;
import com.example.AnonymousChat.util.MessagesText;
import com.example.AnonymousChat.util.UserComparator;
import com.example.AnonymousChat.util.builder.KeyboardBuilder;
import com.example.AnonymousChat.util.builder.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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

            if (currentUser.getPreviousChatId() != null) {
                changeReputation(chatId, text, currentUser);
            }

            switch (text) {
                case "/start" -> processingCmdStart(chatId, currentUser);
                case "/new" -> newConversation(chatId);
                case "/stop" -> stopConversation(chatId);
                case "/share" -> processingCmdShare(currentUser, msg);
            }
        } else if (update != null && update.hasMessage()) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();
            var currentUser = userService.findByChatId(chatId);
            if (currentUser.getOpponentChatId() != null) {
                var opponentChatId = currentUser.getOpponentChatId();
                if (msg.hasPhoto()) {
                    msgSender.sendPhoto(opponentChatId, msg);
                }
                if (msg.hasVideo()) {
                    msgSender.sendVideo(opponentChatId, msg);
                }
                if (msg.hasAnimation()) {
                    msgSender.sendAnimation(opponentChatId, msg);
                }
                if (msg.hasAudio()) {
                    msgSender.sendAudio(opponentChatId, msg);
                }
                if (msg.hasVoice()) {
                    msgSender.sendVoice(opponentChatId, msg);
                }
                if (msg.hasText()) {
                    msgSender.sendText(opponentChatId, msg);
                }
            }
        }
    }


    private synchronized void changeReputation(Long chatId, String text, User user) {
        var opponentChatId = user.getPreviousChatId();
        var opponent = userService.findByChatId(opponentChatId);
        if (opponent != null) {
            var reputation = opponent.getReputation();
            if (text.equals("👍")) {
                opponent.setReputation(reputation + 1);
            } else if (text.equals("👎")) {
                opponent.setReputation(reputation - 1);
            }
            user.setPreviousChatId(null);
            userService.saveAll(List.of(user, opponent));
            msgSender.sendText(chatId, "Thank you for feedback");
        }
        else {
            msgSender.sendMessage(MessageBuilder.messageOfKeyboard(chatId, MessagesText.errorReputation, KeyboardBuilder.reputationItems));
        }
    }

    private synchronized void processingCmdStart(Long chatId, User user) {
        if (user == null) {
            user = User.builder().chatId(chatId).reputation(0L).build();
            userService.save(user);
        }
        msgSender.sendText(chatId, MessagesText.startMessage);
    }

    private synchronized void newConversation(Long chatId) {
        var currentUser = userService.findByChatId(chatId);
        if (busyUsers.contains(currentUser)) {
            users.removeIf(x -> x.getChatId().equals(chatId));
            busyUsers.remove(currentUser);
        }

        msgSender.sendText(chatId, "🔍 Start looking for other user");

        if (users.size() == 0) {
            users.add(currentUser);
        } else {
            var opponent = users.stream().findFirst().orElse(null);
            Session session = Session.builder().firstUser(currentUser).secondUser(opponent).build();

            currentUser.setOpponentChatId(opponent.getChatId());
            opponent.setOpponentChatId(currentUser.getChatId());

            userService.saveAll(List.of(currentUser, opponent));

            users.remove(opponent);
            sessions.add(session);
            busyUsers.addAll(List.of(currentUser, opponent));

            msgSender.sendText(currentUser.getChatId(), MessagesText.startChat);
            msgSender.sendText(opponent.getChatId(), MessagesText.startChat);
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

                firstUser.setPreviousChatId(firstUser.getOpponentChatId());
                secondUser.setPreviousChatId(secondUser.getOpponentChatId());

                firstUser.setOpponentChatId(null);
                secondUser.setOpponentChatId(null);

                userService.saveAll(List.of(firstUser, secondUser));

                var firstChatId = sessionOfUser.getFirstUser().getChatId();
                var secondChatId = sessionOfUser.getSecondUser().getChatId();
                msgSender.sendText(firstChatId, MessagesText.interruptChat);
                msgSender.sendText(secondChatId, MessagesText.interruptChat);

                msgSender.sendMessage(MessageBuilder.messageOfKeyboard(chatId, MessagesText.msgReputation, KeyboardBuilder.reputationItems));
                msgSender.sendMessage(MessageBuilder.messageOfKeyboard(chatId, MessagesText.msgReputation, KeyboardBuilder.reputationItems));
            }
            else if (users.contains(user)) {
                users.remove(user);
                msgSender.sendText(chatId, MessagesText.stopChat);
            }
            else {
                msgSender.sendText(chatId, MessagesText.errorChat);
            }
        }
    }

    private synchronized void processingCmdShare(User currentUser, Message msg) {
        if (currentUser != null && msg != null) {
            if (currentUser.getOpponentChatId() != null) {
                var username = msg.getFrom().getUserName();
                msgSender.sendText(
                        currentUser.getOpponentChatId(),
                        String.format("""
                                🔥 The user shared own username:
                                😎 It's a @%s
                                """, username
                        )
                );
            }
            else {
                msgSender.sendText(currentUser.getChatId(), "👀 Your is not in the chat! 😕");
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
