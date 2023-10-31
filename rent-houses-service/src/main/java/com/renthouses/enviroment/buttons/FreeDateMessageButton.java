package com.renthouses.enviroment.buttons;

import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.services.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class FreeDateMessageButton extends Button{

    private final CalendarService calendarService;

    @SneakyThrows
    @Override
    public EditMessageText editMessage(Update update) {
        //todo Исправить message
        //todo Надо округлить dateTime
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

        String message =
                "Напишите желаемую дату броинрования в формате год-месяц-день. Например: 2023-07-05";
        String[] callbacks = update.getCallbackQuery().getData().split("#");
        DateTime dateTime = DateTime.parse(callbacks[2]);
        List<FreeDateDto> freeDateForAWeek = null;

        if(callbacks[1].equals("back") && DateTime.now().isBefore(dateTime.minusWeeks(1))){
            dateTime = dateTime.minusWeeks(1);
            freeDateForAWeek = calendarService.getFreeDateForAWeek(dateTime);
        } else if(callbacks[1].equals("forward")) {
            dateTime = dateTime.plusWeeks(1);
            freeDateForAWeek = calendarService.getFreeDateForAWeek(dateTime);
        } else {
            freeDateForAWeek = calendarService.getFreeDateForAWeek(DateTime.now());
        }

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
                callback = "DateMessageButton#" +
                        date.getStartDate();
            }
            row.add(InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callback)
                    .build());
            keyboards.add(row);
        }

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("⬅\uFE0F")
                .callbackData("FreeDateMessageButton#"+ "back#" + dateTime)
                .build());

        row.add(InlineKeyboardButton.builder()
                .text("➡\uFE0F")
                .callbackData("FreeDateMessageButton#"+ "forward#" + dateTime)
                .build());
        keyboards.add(row);
        markupLine.setKeyboard(keyboards);

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(message)
                .replyMarkup(markupLine)
                .build();
    }

    @Override
    public boolean support(String cmd) {
        return cmd.startsWith("FreeDateMessageButton");
    }

}
