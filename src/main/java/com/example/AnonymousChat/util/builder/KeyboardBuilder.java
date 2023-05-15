package com.example.AnonymousChat.util.builder;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardBuilder {
    public static List<String> reputationItems = List.of("👍", "👎", "🚫");

    public static ReplyKeyboardMarkup createKeyboardOfList(List<String> buttons) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        buttons.forEach(textButton -> {
            var row = new KeyboardRow();
            row.add(textButton);
            keyboardRows.add(row);
        });
        var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
}
