package com.renthouses.enviroment.buttons;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.renthouses.enviroment.services.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class BookingButton extends Button{

    private final CalendarService calendarService;

    @Override
    public EditMessageText editMessage(Update update) {

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message =
                "Вы успешно забронировали домик.";
        String[] callback = update.getCallbackQuery().getData().split("#");
        int quantityOfDays = Integer.parseInt(callback[1]);

        try {
            calendarService.book(callback[2], quantityOfDays);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        return EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .build();
    }

    @Override
    public boolean support(String cmd) {
        return cmd.startsWith("DateButton");
    }
}
