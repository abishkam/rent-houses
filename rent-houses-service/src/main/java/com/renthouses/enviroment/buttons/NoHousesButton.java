package com.renthouses.enviroment.buttons;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoHousesButton extends Button{

    @Override
    public EditMessageText editMessage(Update update) {

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message =
                "На эту дату нет мест";

        return EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .build();
    }

    @Override
    public boolean support(String cmd) {
        return cmd.startsWith("NoHousesButton");
    }

}
