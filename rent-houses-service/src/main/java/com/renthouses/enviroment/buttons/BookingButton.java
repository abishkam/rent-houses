package com.renthouses.enviroment.buttons;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
public class BookingButton extends Button{

    @Override
    public EditMessageText editMessage(Update update) {

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message =
                "Выберите количество дней на которое вы хотите заехать.";
        String[] callback = update.getCallbackQuery().getData().split("#");
        int quantityOfDays = Integer.parseInt(callback[1]);

        org.joda.time.DateTime dateTime = new org.joda.time.DateTime(new DateTime(callback[2]).getValue());
        dateTime.plusDays(quantityOfDays);
        DateTime date = new DateTime(String.valueOf(dateTime));
        String colorId = callback[3];


        Event event = new Event()
                .setColorId(colorId);

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();


        return EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .replyMarkup(markupLine)
                .build();
    }

    @Override
    public boolean support(String cmd) {
        return cmd.startsWith("DateButton");
    }
}
