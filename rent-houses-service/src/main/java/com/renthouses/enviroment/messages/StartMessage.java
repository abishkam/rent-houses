package com.renthouses.enviroment.messages;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StartMessage extends Message{

    public SendMessage sendMessage(Update update) {
        //todo исправить message
        String chatId = update.getMessage().getChatId().toString();

        String message =
                "Привет! Это бот для бронирования домиков. Нажмите\n\n" +
                        "/booking - забронировать\n" +
                        "/media - посмотреть домики\n" +
                        "/freedate - чтобы узнать свободные дни" +
                        "/help - навигация по боту";

        return new SendMessage(chatId, message);
    }

    @Override
    public boolean support(String cmd) {
        return cmd.equals("/start");
    }
}
