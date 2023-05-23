package com.example.AnonymousChat.handlers;

import com.example.AnonymousChat.model.user.Report;
import com.example.AnonymousChat.model.user.User;
import com.example.AnonymousChat.service.UserService;
import com.example.AnonymousChat.util.message.MessageSender;
import com.example.AnonymousChat.util.message.MessagesText;
import com.example.AnonymousChat.util.UserComparator;
import com.example.AnonymousChat.util.builder.KeyboardBuilder;
import com.example.AnonymousChat.util.builder.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class ChatHandler {
    private final UserService userService;
    private MessageSender msgSender;

    public ChatHandler(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }

    public synchronized void sendTextBetweenUsers(User currentUser, Message msg) {
        if (currentUser != null && currentUser.getOpponentChatId() != null && msg.hasText()) {
            var opponentChatId = currentUser.decryptOpponentChatId();
            msgSender.sendText(opponentChatId, msg);
        }
    }

    public synchronized void processingCmdShare(User currentUser, Message msg) {
        if (currentUser != null && msg != null) {
            if (currentUser.getOpponentChatId() != null) {
                var username = msg.getFrom().getUserName();
                msgSender.sendText(
                        currentUser.decryptOpponentChatId(),
                        String.format("🔥 The user shared own username:\n😎 It's a @%s", username)
                );
            }
            else {
                msgSender.sendText(currentUser.getChatId(), "👀 Your is not in the chat! 😕");
            }
        }
    }

    public synchronized void changeReputationOrBlock(Long chatId, String text, User user) {
        if (user == null) return;

        var opponentChatId = user.decryptPreviousChatId();
        var opponent = userService.findByChatId(opponentChatId);

        if (opponent != null) {
            var reputation = opponent.getReputation();
            switch (text) {
                case "👍" -> {
                    opponent.setReputation(reputation + 1);
                    user.setPreviousChatId(null);
                    msgSender.sendText(chatId, MessagesText.THANK_YOU_FOR_FEEDBACK);
                }
                case "👎" -> {
                    opponent.setReputation(reputation - 1);
                    user.setPreviousChatId(null);
                    msgSender.sendText(chatId, MessagesText.THANK_YOU_FOR_FEEDBACK);
                }
                case "🚫" -> {
                    var list = Arrays.stream(Report.values())
                            .toList();

                    var stringList = list.stream()
                            .map(Enum::name)
                            .toList();

                    msgSender.sendMessage(MessageBuilder.msgOfKeyboard(chatId, MessagesText.REPORTS_OF_THE_USER, stringList));
                }
                case "🙅‍♂️" -> {
                    user.getBlockUsers().add(opponent);
                    user.setPreviousChatId(null);
                    msgSender.sendText(chatId, MessagesText.THANK_YOU_FOR_FEEDBACK);
                }
                default -> {
                    try {
                        if (!user.getBlockUsers().contains(opponent)) {
                            Report report = Report.valueOf(text);
                            opponent.getReports().add(report);
                            userService.save(opponent);
                            user.setPreviousChatId(null);
                            user.getBlockUsers().add(opponent);
                        }
                    } catch (Exception e) {
                        var list = Arrays.stream(Report.values())
                                .toList();

                        var stringList = list.stream()
                                .map(Enum::name)
                                .toList();

                        msgSender.sendMessage(MessageBuilder.msgOfKeyboard(chatId, MessagesText.WRONG_FORMAT_OF_THE_REPORT_PLEASE_TRY_AGAIN, stringList));
                    }
                }
            }
            userService.saveAll(List.of(user, opponent));

            SendMessage message = new SendMessage(String.valueOf(chatId), MessagesText.THANK_YOU_FOR_FEEDBACK);
            message.setReplyMarkup(new ReplyKeyboardRemove());

            msgSender.sendMessage(message);
        }
        else {
            msgSender.sendMessage(MessageBuilder.msgOfKeyboard(chatId, MessagesText.WRONG_FORMAT_OF_ASSESSMENT_PLEASE_TRY_AGAIN, KeyboardBuilder.reputationItems));
        }
    }

    public synchronized void newConversation(Long chatId, Set<User> users) {
        var currentUser = userService.findByChatId(chatId);
        if (users.contains(currentUser)) users.removeIf(x -> x.getChatId().equals(chatId));

        msgSender.sendText(chatId, MessagesText.LOOKING_FOR_OTHER_USER);

        if (users.size() == 0) {
            users.add(currentUser);
        } else {
            var availUsers = new ConcurrentSkipListSet<>(new UserComparator());
            while (!users.isEmpty()) {
                if (availUsers.size() == 0) {
                    availUsers = new ConcurrentSkipListSet<>(new UserComparator());
                    availUsers.addAll(users);
                }
                else {
                    var opponent = availUsers.pollFirst();
                    if (opponent != null) {
                        List<User> blockedUsers = new ArrayList<>();
                        blockedUsers.addAll(currentUser.getBlockUsers());
                        blockedUsers.addAll(opponent.getBlockUsers());

                        var blockedUserId = blockedUsers.stream().map(User::getId).toList();

                        if (!blockedUserId.contains(currentUser.getId()) && !blockedUserId.contains(opponent.getId())) {
                            currentUser.setOpponentChatId(opponent.encryptOwnChatId());
                            opponent.setOpponentChatId(currentUser.encryptOwnChatId());
                            userService.saveAll(List.of(currentUser, opponent));
                            users.remove(opponent);
                            msgSender.sendMessageForTwoUsers(currentUser.getChatId(), opponent.getChatId(), MessagesText.START_CHAT);
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void stopConversation(Long chatId, Set<User> users) {
        var user = userService.findByChatId(chatId);
        if (user != null) {
            var opponentId = user.decryptOpponentChatId();

            if (opponentId != null) {

                var firstUser = userService.findByChatId(chatId);
                var secondUser = userService.findByChatId(opponentId);

                firstUser.setPreviousChatId(firstUser.getOpponentChatId());
                secondUser.setPreviousChatId(secondUser.getOpponentChatId());

                firstUser.setOpponentChatId(null);
                secondUser.setOpponentChatId(null);

                userService.saveAll(List.of(firstUser, secondUser));

                var firstChatId = firstUser.getChatId();
                var secondChatId = secondUser.getChatId();

                msgSender.sendMessageForTwoUsers(firstChatId, secondChatId, MessagesText.STOP_CHAT);

                msgSender.sendMessage(MessageBuilder.msgOfKeyboard(chatId, MessagesText.RATER_CHAT_WITH_THIS_OPPONENT, KeyboardBuilder.reputationItems));
                msgSender.sendMessage(MessageBuilder.msgOfKeyboard(opponentId, MessagesText.RATER_CHAT_WITH_THIS_OPPONENT, KeyboardBuilder.reputationItems));
            }
            else if (users.contains(user)) {
                users.remove(user);
                msgSender.sendText(chatId, MessagesText.LOOKING_FOR_CHAT_WAS_STOPPED);
            }
            else {
                msgSender.sendText(chatId, MessagesText.ERROR_CHAT);
            }
        }
    }
}
