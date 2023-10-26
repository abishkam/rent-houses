package com.renthouses.enviroment.buttons;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultButton extends Button {

    @Override
    public EditMessageText editMessage(Update update) {
        //todo Подправить message
        String chatId = update.getMessage().getChatId().toString();
        String message =
                "";
        return new EditMessageText(message);
    }

    @Override
    public boolean support(String cmd) {
        return true;
    }
}
