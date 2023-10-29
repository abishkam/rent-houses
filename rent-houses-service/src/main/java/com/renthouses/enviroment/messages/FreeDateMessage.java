package com.renthouses.enviroment.messages;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FreeDateMessage extends Message{

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
        return cmd.equals("/freedate");
    }
}
