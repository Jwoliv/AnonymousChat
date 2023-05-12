package com.example.AnonymousChat.util;

import com.example.AnonymousChat.bot.AnonChatBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageSender {
    private final AnonChatBot anonChatBot;

    public MessageSender(AnonChatBot anonChatBot) {
        this.anonChatBot = anonChatBot;
    }


    public synchronized void sendPhoto(Long opponentChatId, String content) {
        SendPhoto photo = SendPhoto.builder().chatId(opponentChatId).photo(new InputFile(content)).build();
        try {
            anonChatBot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendAnimation(Long opponentChatId, String content) {
        SendAnimation animation = SendAnimation.builder().chatId(opponentChatId).animation(new InputFile(content)).build();
        try {
            anonChatBot.execute(animation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendVideo(Long opponentChatId, String content) {
        SendVideo forwardMessage = SendVideo.builder().chatId(opponentChatId).video(new InputFile(content)).build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendAudio(Long opponentChatId, String content) {
        SendAudio forwardMessage = SendAudio.builder().chatId(opponentChatId).audio(new InputFile(content)).build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendVoice(Long opponentChatId, String content) {
        SendVoice forwardMessage = SendVoice.builder().chatId(opponentChatId).voice(new InputFile(content)).build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendText(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        try {
            anonChatBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
