package com.example.AnonymousChat.util;

import com.example.AnonymousChat.bot.AnonChatBot;
import com.example.AnonymousChat.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class MessageSender {
    private final AnonChatBot anonChatBot;

    public MessageSender(AnonChatBot anonChatBot) {
        this.anonChatBot = anonChatBot;
    }


    public synchronized void sendBetweenUsers(User currentUser, Message msg) {
        if (currentUser == null || msg == null) return;

        var opponentChatId = currentUser.decryptOpponentChatId();

        if (msg.hasPhoto()) {
            sendPhoto(opponentChatId, msg);
        } else if (msg.hasVideo()) {
            sendVideo(opponentChatId, msg);
        } else if (msg.hasAnimation()) {
            sendAnimation(opponentChatId, msg);
        } else if (msg.hasAudio()) {
            sendAudio(opponentChatId, msg);
        } else if (msg.hasVoice()) {
            sendVoice(opponentChatId, msg);
        } else if (msg.hasText()) {
            sendText(opponentChatId, msg);
        }
    }

    public synchronized void sendPhoto(Long opponentChatId, Message msg) {
        if (msg == null) return;

        SendPhoto photo = SendPhoto.builder()
                .chatId(opponentChatId)
                .photo(new InputFile(msg.getPhoto().get(0).getFileId()))
                .caption(msg.getCaption())
                .build();

        try {
            anonChatBot.execute(photo);
            log.info("{} sendPhoto(): Photo sent to chat ID: {}", this.getClass(), opponentChatId);
        } catch (TelegramApiException e) {
            log.error("{} sendPhoto(): Error sending photo to chat ID: {}", this.getClass(), opponentChatId);
        }
    }

    public synchronized void sendMessageForTwoUsers(Long chatIdFirst, Long chatIdSecond, String messageText) {
        sendText(chatIdFirst, messageText);
        sendText(chatIdSecond, messageText);
    }

    public synchronized void sendAnimation(Long opponentChatId, Message msg) {
        if (msg == null) return;

        SendAnimation animation = SendAnimation.builder()
                .chatId(opponentChatId)
                .animation(new InputFile(msg.getAnimation().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(animation);
            log.info("{} sendAnimation(): Animation sent to chat ID: {}", this.getClass(), opponentChatId);
        } catch (TelegramApiException e) {
            log.error("{} sendAnimation(): Error sending animation to chat ID: {}", this.getClass(), opponentChatId);
        }
    }

    public synchronized void sendVideo(Long opponentChatId, Message msg) {
        if (msg == null) return;

        SendVideo video = SendVideo.builder()
                .chatId(opponentChatId)
                .video(new InputFile(msg.getVideo().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(video);
            log.info("{} sendVideo(): Video sent to chat ID: {}", this.getClass(), opponentChatId);
        } catch (TelegramApiException e) {
            log.error("{} sendVideo(): Error sending video to chat ID: {}", this.getClass(), opponentChatId);
        }
    }

    public synchronized void sendAudio(Long opponentChatId, Message msg) {
        if (msg == null) return;

        SendAudio audio = SendAudio.builder()
                .chatId(opponentChatId)
                .audio(new InputFile(msg.getAudio().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(audio);
            log.info("{} sendAudio(): Audio sent to chat ID: {}", this.getClass(), opponentChatId);
        } catch (TelegramApiException e) {
            log.error("{} sendAudio(): Error sending audio to chat ID: {}", this.getClass(), opponentChatId);
        }
    }

    public synchronized void sendVoice(Long opponentChatId, Message msg) {
        if (msg == null) return;

        SendVoice voice = SendVoice.builder()
                .chatId(opponentChatId)
                .voice(new InputFile(msg.getVoice().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(voice);
            log.info("{} sendVoice(): Voice sent to chat ID: {}", this.getClass(), opponentChatId);
        } catch (TelegramApiException e) {
            log.error("{} sendVoice(): Error sending voice to chat ID: {}", this.getClass(), opponentChatId);
        }
    }

    public synchronized void sendText(long chatId, Message msg) {
        if (msg == null) return;

        SendMessage text = new SendMessage(String.valueOf(chatId), msg.getText());
        try {
            anonChatBot.execute(text);
            log.info("{} sendText(): Text sent to chat ID: {}", this.getClass(), chatId);
        } catch (TelegramApiException e) {
            log.error("{} sendText(): Error sending text to chat ID: {}", this.getClass(), chatId);
        }
    }

    public synchronized void sendText(long chatId, String txt) {
        SendMessage text = new SendMessage(String.valueOf(chatId), txt);
        try {
            anonChatBot.execute(text);
            log.info("{} sendText(): Text sent to chat ID: {}", this.getClass(), chatId);
        } catch (TelegramApiException e) {
            log.error("{} sendText(): Error sending text to chat ID: {}", this.getClass(), chatId);
        }
    }

    public synchronized void sendMessage(SendMessage sendMessage) {
        try {
            anonChatBot.execute(sendMessage);
            log.info("{} sendMessage(): Message sent to chat ID: {}", this.getClass(), sendMessage.getChatId());
        } catch (TelegramApiException e) {
            log.error("{} sendMessage(): Error sending message to chat ID: {}", this.getClass(), sendMessage.getChatId());
        }
    }

}
