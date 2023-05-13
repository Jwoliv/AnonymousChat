package com.example.AnonymousChat.util;

import com.example.AnonymousChat.bot.AnonChatBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageSender {
    private final AnonChatBot anonChatBot;

    public MessageSender(AnonChatBot anonChatBot) {
        this.anonChatBot = anonChatBot;
    }


    public synchronized void sendMedia() {

    }

    public synchronized void sendPhoto(Long opponentChatId, Message msg) {
        SendPhoto photo = SendPhoto.builder()
                .chatId(opponentChatId)
                .photo(new InputFile(msg.getPhoto().get(0).getFileId()))
                .caption(msg.getCaption())
                .build();

        try {
            anonChatBot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendAnimation(Long opponentChatId, Message msg) {
        SendAnimation animation = SendAnimation.builder()
                .chatId(opponentChatId)
                .animation(new InputFile(msg.getAnimation().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(animation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendVideo(Long opponentChatId, Message msg) {
        SendVideo forwardMessage = SendVideo.builder()
                .chatId(opponentChatId)
                .video(new InputFile(msg.getVideo().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendAudio(Long opponentChatId, Message msg) {
        SendAudio forwardMessage = SendAudio.builder()
                .chatId(opponentChatId)
                .audio(new InputFile(msg.getAudio().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendVoice(Long opponentChatId, Message msg) {
        SendVoice forwardMessage = SendVoice.builder()
                .chatId(opponentChatId)
                .voice(new InputFile(msg.getVoice().getFileId()))
                .caption(msg.getCaption())
                .build();
        try {
            anonChatBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendText(long chatId, Message msg) {
        SendMessage message = new SendMessage(String.valueOf(chatId), msg.getText());
        try {
            anonChatBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendText(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            anonChatBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(SendMessage sendMessage) {
        try {
            anonChatBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
