package com.renthouses.enviroment.messages;

import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.services.CalendarService;
import com.renthouses.enviroment.util.RowUtil;
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

    private final CalendarService calendarService;
    private final RowUtil rowUtil;

    @Override
    public SendMessage sendMessage(Update update) throws ParseException {
        //todo Sneakythrows

        String chatId = update.getMessage().getChatId().toString();
        String startDate = update.getMessage().getText();

        try {
            FreeDateDto dto =
                    calendarService.getFreeDate(startDate);

            if(dto.isFree()){
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(dto.getMessage())
                        .replyMarkup(rowUtil.createRows(dto))
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
