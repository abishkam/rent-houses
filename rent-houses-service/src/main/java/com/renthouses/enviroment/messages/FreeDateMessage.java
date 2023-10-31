package com.renthouses.enviroment.messages;

import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.services.CalendarService;
import com.renthouses.enviroment.util.RowUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class FreeDateMessage extends Message{

    private final CalendarService service;
    private final RowUtil rowUtil;

    @SneakyThrows
    @Override
    public SendMessage sendMessage(Update update) {
        //todo Исправить message
        String chatId = update.getMessage().getChatId().toString();

        String message =
                "Напишите желаемую дату броинрования в формате год-месяц-день. Например: 2023-07-05";

        List<FreeDateDto> freeDateForAWeek = service.getFreeDateForAWeek(DateTime.now());

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboards = new ArrayList<>();
        String text;
        String callback;
        for (FreeDateDto date:
             freeDateForAWeek) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            if(date.getMessage().startsWith("На эту дату все домики заняты")) {
                text = date.getStartDate().toString().substring(5,10) + " : " + date.getMessage();
                callback = "NoHousesButton#" +
                        date.getStartDate();
            } else {
                text = date.getStartDate().toString().substring(5,10) + " : " + date.getMessage().substring(0, 30);
                callback = "FreeDateMessage#" +
                        date.getStartDate()+"#";
            }
            row.add(InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callback)
                    .build());
            keyboards.add(row);
        }

        markupLine.setKeyboard(keyboards);
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(markupLine)
                .build();
    }

    @Override
    public boolean support(String cmd) {
        return cmd.equals("/freedate");
    }
}
