package com.renthouses.enviroment.messages;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultMessage extends Message{

    @Override
    public SendMessage sendMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();

        return new SendMessage(chatId, "Выберите команду");
    }

    @Override
    public boolean support(String cmd) {
        return true;
    }

}
