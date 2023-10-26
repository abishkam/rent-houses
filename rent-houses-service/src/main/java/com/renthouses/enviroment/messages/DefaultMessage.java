package com.renthouses.enviroment.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DefaultMessage extends Message{

    @Override
    public SendMessage sendMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();

        return new SendMessage(chatId, "Выберите команду");
    }

    @Override
    public boolean support(String cmd) {
        return false;
    }

}
