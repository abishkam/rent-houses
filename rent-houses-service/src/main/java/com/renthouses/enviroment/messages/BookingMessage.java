package com.renthouses.enviroment.messages;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class BookingMessage extends Message{

    @Override
    public SendMessage sendMessage(Update update) {
        //todo Исправить message
        String chatId = update.getMessage().getChatId().toString();

        String message =
                "Напишите желаемую дату броинрования в формате год-месяц-день. Например: 2023-07-05";

        return new SendMessage(chatId, message);
    }

    @Override
    public boolean support(String cmd) {
        return cmd.equals("/booking");
    }
}
