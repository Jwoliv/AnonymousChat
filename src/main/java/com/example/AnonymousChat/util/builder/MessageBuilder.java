package com.example.AnonymousChat.util.builder;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class MessageBuilder {
    public static SendMessage messageOfKeyboard(Long chatId, String title, List<String> buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(title);
        sendMessage.setReplyMarkup(KeyboardBuilder.createKeyboardOfList(buttons));
        return sendMessage;
    }
}
