package com.renthouses.enviroment.messages;

import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.services.CalendarApiConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DateMessage extends Message{

    private final CalendarApiConfiguration calendarApiConfiguration;

    @Override
    public SendMessage sendMessage(Update update) throws ParseException {
        //todo Sneakythrows

        String chatId = update.getMessage().getChatId().toString();
        String startDate = update.getMessage().getText();

        try {
            FreeDateDto dto =
                    calendarApiConfiguration.getFreeDate(startDate);

            if(dto.isFree()){

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = format.parse(startDate);
                Calendar startDateCalendar = Calendar.getInstance();
                startDateCalendar.setTime(date1);

                // Получаем текущую дату
                Calendar finishDate = Calendar.getInstance();
                finishDate.setTime(new Date(dto.getFreeDate().getValue()));
                // Вычисляем разницу между датами в миллисекундах
                long differenceInMillis = finishDate.getTimeInMillis() - startDateCalendar.getTimeInMillis();

                // Конвертируем разницу в дни
                int quantityOfDays = (int) Math.min(differenceInMillis / (24 * 60 * 60 * 1000), 30);

                int nCol = (int) Math.ceil(quantityOfDays/10.0);
                int[] c = new int[nCol+1];//оставляю 1-й элемент равным 0

                InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
                List<InlineKeyboardButton> rows = new ArrayList<>();

                for (int i = 1; i < c.length; i++) {

                    c[i] =c[i-1] + Math.min(quantityOfDays, 10);
                    rows.add(InlineKeyboardButton.builder()
                            .text("до " + c[i])
                            .callbackData("DateMessage#"
                                    + c[i]+"#"
                                    +dto.getFreeDate()+"#"
                                    +dto.getColorId())
                            .build());
                    quantityOfDays-=10;
                }
                markupLine.setKeyboard(Collections.singletonList(rows));

                return SendMessage.builder()
                        .chatId(chatId)
                        .text(dto.getMessage())
                        .replyMarkup(markupLine)
                .build();

            } else {
                return new SendMessage(chatId, dto.getMessage());
            }

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        } 

    }

    @Override
    public boolean support(String cmd) {

        String regex = "(202[3-5])-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cmd);

        return matcher.matches();
    }
}
