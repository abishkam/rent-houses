package com.renthouses.enviroment.buttons;

import com.google.api.client.util.DateTime;
import com.renthouses.enviroment.services.CalendarApiConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DateButton extends Button {

    @Override
    public EditMessageText editMessage(Update update) {
        //todo Сделать bold шрифт
        //todo Дополнить message
        //todo реализовать SendMessage в Message

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message =
                "Выберите количество дней на которое вы хотите заехать.";
        String[] callback = update.getCallbackQuery().getData().split("#");
        int quantityOfDays = Integer.parseInt(callback[1]);
        String dateTime = callback[2];
        String colorId = callback[3];
        int closestMultipleOfTen = quantityOfDays%10==0? quantityOfDays-9:((quantityOfDays / 10) * 10)+1;

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rows1 = new ArrayList<>();
        List<InlineKeyboardButton> rows2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboards = new ArrayList<>();

        IntStream.rangeClosed(closestMultipleOfTen, quantityOfDays)
                .sorted()
                .forEach(i -> {
                    if (i%10<=5 && i%10!=1) {

                        rows1.add(InlineKeyboardButton.builder()
                                .text("" + i)
                                .callbackData("DateButton#" + i + "#" + dateTime + "#" + colorId)
                                .build());
                    } else {

                        rows2.add(InlineKeyboardButton.builder()
                                .text("" + i)
                                .callbackData("DateButton#" + i + "#" + dateTime + "#" + colorId)
                                .build());
                    }
                });

        keyboards.add(rows1);
        keyboards.add(rows2);
        markupLine.setKeyboard(keyboards);

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
        //todo Написать pattern
        return cmd.startsWith("DateMessage");
    }
}
